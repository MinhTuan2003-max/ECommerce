package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.dtos.payment.request.SepayWebhookRequest;
import fpt.tuanhm43.server.dtos.payment.response.PaymentResponse;
import fpt.tuanhm43.server.dtos.payment.response.PaymentStatusResponse;
import fpt.tuanhm43.server.entities.Order;
import fpt.tuanhm43.server.entities.PaymentTransaction;
import fpt.tuanhm43.server.enums.PaymentMethod;
import fpt.tuanhm43.server.enums.PaymentStatus;
import fpt.tuanhm43.server.exceptions.BadRequestException;
import fpt.tuanhm43.server.exceptions.ResourceNotFoundException;
import fpt.tuanhm43.server.repositories.OrderRepository;
import fpt.tuanhm43.server.repositories.PaymentTransactionRepository;
import fpt.tuanhm43.server.services.InventoryService;
import fpt.tuanhm43.server.services.MailService;
import fpt.tuanhm43.server.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final InventoryService inventoryService;
    private final MailService mailService;

    @Value("${app.payment.sepay.webhook-key:}")
    private String sepayWebhookKey;

    @Value("${app.frontend.url:http://localhost:8080/payment-demo}")
    private String frontendUrl;

    @Override
    @Transactional
    public PaymentStatusResponse initiatePayment(UUID orderId, PaymentMethod method) {
        log.info("Initiating payment for order: {}, Method: {}", orderId, method);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Validate order status
        if (order.isPaymentCompleted()) {
            throw new BadRequestException("Order payment is already completed");
        }

        // Create payment transaction
        String transactionId = generateTransactionId();
        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionId(transactionId)
                .order(order)
                .method(method)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .providerName(method.name())
                .build();

        paymentTransactionRepository.save(transaction);

        // Based on payment method, prepare response
        if (method == PaymentMethod.COD) {
            return PaymentStatusResponse.builder()
                    .transactionId(transactionId)
                    .status(PaymentStatus.PENDING)
                    .message("COD payment - No online payment required")
                    .build();
        } else if (method == PaymentMethod.SEPAY) {
            // Generate SePay payment link
            String paymentUrl = generateSepayPaymentUrl(order, transactionId);
            log.info("SePay payment URL generated for transaction: {}", transactionId);

            return PaymentStatusResponse.builder()
                    .transactionId(transactionId)
                    .status(PaymentStatus.PROCESSING)
                    .message("Redirect to SePay for payment")
                    .paymentUrl(paymentUrl)
                    .build();
        }

        throw new BadRequestException("Unsupported payment method: " + method);
    }

    @Override
    @Transactional
    public void handleSepayWebhook(SepayWebhookRequest request) {
        log.info("Handling SePay webhook - Transaction: {}", request.getTransactionId());

        if ("dev-bypass".equals(request.getSignature())) {
            log.warn("Demo mode: Skipping signature verification for transaction: {}", request.getTransactionId());
        }
        else {
            String dataJson = request.getStatus();
            if (!verifyWebhookSignature(request.getSignature(), dataJson)) {
                log.warn("Invalid webhook signature for transaction: {}", request.getTransactionId());
                throw new BadRequestException("Invalid webhook signature");
            }
        }

        // Find payment transaction
        PaymentTransaction transaction = paymentTransactionRepository
                .findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentTransaction", "transactionId", request.getTransactionId()));

        // Check idempotency - if already processed, skip
        if (transaction.getStatus() == PaymentStatus.PAID
                || transaction.getStatus() == PaymentStatus.FAILED) {
            log.debug("Webhook already finalized for transaction: {}", request.getTransactionId());
            return;
        }


        Order order = transaction.getOrder();

        // Handle based on status
        if ("SUCCESS".equalsIgnoreCase(request.getStatus())) {
            // Payment successful
            log.info("Payment successful for transaction: {}", request.getTransactionId());

            transaction.setStatus(PaymentStatus.PAID);

            // Extract reference code from data if available
            if (request.getData() != null && request.getData().containsKey("referenceCode")) {
                transaction.setProviderReference((String) request.getData().get("referenceCode"));
            }

            paymentTransactionRepository.save(transaction);

            order.markAsPaid();
            orderRepository.save(order);

            // Deduct reserved inventory
            try {
                inventoryService.deductReservedStock(order.getId());
                log.info("Inventory deducted for order: {}", order.getId());
            } catch (Exception e) {
                log.error("Failed to deduct inventory for order: {}", order.getId(), e);
            }

            // Send payment confirmation email
            try {
                mailService.sendPaymentConfirmation(order);
            } catch (Exception e) {
                log.warn("Failed to send payment confirmation email for order: {}", order.getId(), e);
            }
        } else {
            // Payment failed
            log.warn("Payment failed for transaction: {} - Status: {}",
                    request.getTransactionId(), request.getStatus());

            transaction.setStatus(PaymentStatus.FAILED);

            // Extract failure reason from data if available
            if (request.getData() != null && request.getData().containsKey("message")) {
                transaction.setFailureReason((String) request.getData().get("message"));
            }

            paymentTransactionRepository.save(transaction);

            // Release inventory reservation
            try {
                inventoryService.releaseReservationByOrder(order.getId());
                log.info("Inventory released for failed payment order: {}", order.getId());
            } catch (Exception e) {
                log.warn("Failed to release inventory for order: {}", order.getId(), e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatus(String transactionId) {
        log.info("Getting payment status for transaction: {}", transactionId);

        PaymentTransaction transaction = paymentTransactionRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentTransaction", "transactionId", transactionId));

        return PaymentStatusResponse.builder()
                .transactionId(transactionId)
                .status(transaction.getStatus())
                .message(getStatusMessage(transaction.getStatus()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrder(UUID orderId) {
        log.info("Getting payment for order: {}", orderId);

        PaymentTransaction transaction = paymentTransactionRepository
                .findLatestByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentTransaction", "orderId", orderId));

        return mapToPaymentResponse(transaction);
    }

    @Override
    @Transactional
    public void processCODPayment(UUID orderId) {
        log.info("Processing COD payment for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getPaymentMethod() != PaymentMethod.COD) {
            throw new BadRequestException("Order is not a COD payment");
        }

        // Find or create payment transaction
        PaymentTransaction transaction = paymentTransactionRepository
                .findLatestByOrderId(orderId)
                .orElseGet(() -> {
                    PaymentTransaction newTransaction = PaymentTransaction.builder()
                            .transactionId(generateTransactionId())
                            .order(order)
                            .method(PaymentMethod.COD)
                            .amount(order.getTotalAmount())
                            .status(PaymentStatus.PENDING)
                            .providerName(PaymentMethod.COD.name())
                            .build();
                    return paymentTransactionRepository.save(newTransaction);
                });

        // Mark as paid
        transaction.setStatus(PaymentStatus.PAID);
        paymentTransactionRepository.save(transaction);

        order.markAsPaid();
        orderRepository.save(order);

        // Deduct reserved inventory
        try {
            inventoryService.deductReservedStock(orderId);
            log.info("Inventory deducted for COD order: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to deduct inventory for COD order: {}", orderId, e);
        }

        // Send payment confirmation email
        try {
            mailService.sendPaymentConfirmation(order);
        } catch (Exception e) {
            log.warn("Failed to send payment confirmation email for order: {}", orderId, e);
        }

        log.info("COD payment processed successfully for order: {}", orderId);
    }

    @Override
    public boolean verifyWebhookSignature(String signature, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    sepayWebhookKey.getBytes(StandardCharsets.UTF_8),
                    0,
                    sepayWebhookKey.getBytes(StandardCharsets.UTF_8).length,
                    "HmacSHA256"
            );
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String computedSignature = Base64.getEncoder().encodeToString(hash);

            return computedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Generate transaction ID
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generate SePay payment URL
     */
    private String generateSepayPaymentUrl(Order order, String transactionId) {
        return String.format("%s/sepay?transactionId=%s&orderId=%s&amount=%s",
                frontendUrl, transactionId, order.getId(), order.getTotalAmount());
    }

    /**
     * Get status message
     */
    private String getStatusMessage(PaymentStatus status) {
        return switch (status) {
            case PENDING -> "Payment pending";
            case PROCESSING -> "Payment processing";
            case PAID -> "Payment completed successfully";
            case FAILED -> "Payment failed";
            case REFUNDED -> "Payment refunded";
        };
    }

    /**
     * Map PaymentTransaction entity to PaymentResponse DTO
     */
    private PaymentResponse mapToPaymentResponse(PaymentTransaction transaction) {
        return PaymentResponse.builder()
                .id(transaction.getId())
                .orderId(transaction.getOrder().getId())
                .transactionId(transaction.getTransactionId())
                .method(transaction.getMethod())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .providerName(transaction.getMethod().name())
                .failureReason(transaction.getFailureReason())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

