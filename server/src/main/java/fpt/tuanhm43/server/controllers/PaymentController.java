package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.payment.request.SepayWebhookRequest;
import fpt.tuanhm43.server.dtos.payment.response.PaymentResponse;
import fpt.tuanhm43.server.dtos.payment.response.PaymentStatusResponse;
import fpt.tuanhm43.server.enums.PaymentMethod;
import fpt.tuanhm43.server.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "Endpoints for handling order payments, SePay integration, and transaction status tracking.")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}/initiate")
    @Operation(summary = "Initiate payment", description = "Starts the payment process. Supports Guest Checkout.")
    public ResponseEntity<ApiResponseDTO<PaymentStatusResponse>> initiatePayment(
            @Parameter(description = "ID of the order to pay") @PathVariable("orderId") UUID orderId,
            @Parameter(description = "Method of payment", example = "SEPAY") @RequestParam("method") PaymentMethod method) {
        log.info("Initiating payment for order: {}, Method: {}", orderId, method);
        PaymentStatusResponse response = paymentService.initiatePayment(orderId, method);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(response, "Payment initiated successfully"));
    }

    @GetMapping("/status/{transactionId}")
    @Operation(summary = "Check payment status", description = "Retrieve the current status of a payment using the bank transaction ID or internal transaction reference.")
    public ResponseEntity<ApiResponseDTO<PaymentStatusResponse>> getPaymentStatus(
            @Parameter(description = "Transaction ID from payment gateway") @PathVariable("transactionId") String transactionId) {
        log.info("Fetching payment status for transaction: {}", transactionId);
        PaymentStatusResponse response = paymentService.getPaymentStatus(transactionId);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by Order ID", description = "Fetch payment details for a specific order. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaymentResponse>> getPaymentByOrder(
            @PathVariable("orderId") UUID orderId) {
        log.info("Fetching payment for order: {}", orderId);
        PaymentResponse response = paymentService.getPaymentByOrder(orderId);
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @PostMapping("/{orderId}/cod/process")
    @Operation(summary = "Process COD Payment", description = "Marks a COD payment as completed. Usually called by Admin when the order is successfully delivered.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> processCODPayment(
            @PathVariable("orderId") UUID orderId) {
        log.info("Processing COD payment for order: {}", orderId);
        paymentService.processCODPayment(orderId);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "COD payment processed successfully"));
    }

    @PostMapping("/webhook/sepay")
    @Operation(summary = "SePay Webhook Listener", description = "Public endpoint used by SePay to notify our system about successful bank transfers. Do not call this manually unless testing.")
    @ApiResponse(responseCode = "200", description = "Webhook received and processed")
    public ResponseEntity<ApiResponseDTO<Void>> handleSepayWebhook(
            @Valid @RequestBody SepayWebhookRequest request) {
        log.info("Received SePay webhook for transaction: {}", request.getTransactionId());
        paymentService.handleSepayWebhook(request);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Webhook processed successfully"));
    }

    @PostMapping("/verify-signature")
    @Operation(summary = "Verify Signature", description = "Security utility to check if the SePay webhook data is authentic and hasn't been tampered with.")
    public ResponseEntity<ApiResponseDTO<Boolean>> verifySignature(
            @RequestParam("signature") String signature,
            @RequestParam("data") String data) {
        log.info("Verifying webhook signature");
        boolean isValid = paymentService.verifyWebhookSignature(signature, data);
        return ResponseEntity.ok(ApiResponseDTO.success(isValid));
    }
}