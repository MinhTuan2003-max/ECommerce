package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.payment.request.SepayWebhookRequest;
import fpt.tuanhm43.server.dtos.payment.response.PaymentResponse;
import fpt.tuanhm43.server.dtos.payment.response.PaymentStatusResponse;
import fpt.tuanhm43.server.enums.PaymentMethod;
import fpt.tuanhm43.server.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Payment Controller
 * Handles payment initiation, status checks, and webhooks
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Initiate payment for an order
     * Supports COD and SEPAY payment methods
     */
    @PostMapping("/{orderId}/initiate")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponseDTO<PaymentStatusResponse>> initiatePayment(
            @PathVariable("orderId") UUID orderId,
            @RequestParam("method") PaymentMethod method) {
        log.info("Initiating payment for order: {}, Method: {}", orderId, method);
        PaymentStatusResponse response = paymentService.initiatePayment(orderId, method);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(response, "Payment initiated successfully"));
    }

    /**
     * Get payment status by transaction ID
     * Can be called by authenticated users or guests with transaction ID
     */
    @GetMapping("/status/{transactionId}")
    public ResponseEntity<ApiResponseDTO<PaymentStatusResponse>> getPaymentStatus(
            @PathVariable("transactionId") String transactionId) {
        log.info("Fetching payment status for transaction: {}", transactionId);
        PaymentStatusResponse response = paymentService.getPaymentStatus(transactionId);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * Get payment details by order ID
     * Admin only
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaymentResponse>> getPaymentByOrder(
            @PathVariable("orderId") UUID orderId) {
        log.info("Fetching payment for order: {}", orderId);
        PaymentResponse response = paymentService.getPaymentByOrder(orderId);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    /**
     * Process COD (Cash on Delivery) payment
     * Called when order is delivered
     */
    @PostMapping("/{orderId}/cod/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> processCODPayment(
            @PathVariable("orderId") UUID orderId) {
        log.info("Processing COD payment for order: {}", orderId);
        paymentService.processCODPayment(orderId);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "COD payment processed successfully"));
    }

    /**
     * SePay Webhook endpoint
     * Called by SePay payment gateway when payment is completed
     * Public endpoint - no authentication required
     */
    @PostMapping("/webhook/sepay")
    public ResponseEntity<ApiResponseDTO<Void>> handleSepayWebhook(
            @Valid @RequestBody SepayWebhookRequest request) {
        log.info("Received SePay webhook for transaction: {}", request.getTransactionId());
        paymentService.handleSepayWebhook(request);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Webhook processed successfully"));
    }

    /**
     * Test webhook endpoint (for development only)
     */
    @PostMapping("/webhook/sepay/test")
    public ResponseEntity<ApiResponseDTO<Void>> testSepayWebhook(
            @Valid @RequestBody SepayWebhookRequest request) {
        log.info("Testing SePay webhook with transaction: {}", request.getTransactionId());
        paymentService.handleSepayWebhook(request);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Test webhook processed successfully"));
    }

    /**
     * Verify webhook signature
     * Used for testing signature validation
     */
    @PostMapping("/verify-signature")
    public ResponseEntity<ApiResponseDTO<Boolean>> verifySignature(
            @RequestParam("signature") String signature,
            @RequestParam("data") String data) {
        log.info("Verifying webhook signature");
        boolean isValid = paymentService.verifyWebhookSignature(signature, data);
        return ResponseEntity.ok(ApiResponseDTO.success(isValid));
    }
}

