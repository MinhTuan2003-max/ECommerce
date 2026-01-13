package fpt.tuanhm43.server.services;

import fpt.tuanhm43.server.dtos.payment.request.SepayWebhookRequest;
import fpt.tuanhm43.server.dtos.payment.response.PaymentResponse;
import fpt.tuanhm43.server.dtos.payment.response.PaymentStatusResponse;
import fpt.tuanhm43.server.enums.PaymentMethod;

import java.util.UUID;

/**
 * Payment Service Interface
 */
public interface PaymentService {

    /**
     * Initiate payment
     */
    PaymentStatusResponse initiatePayment(UUID orderId, PaymentMethod method);

    /**
     * Handle SePay webhook
     */
    void handleSepayWebhook(SepayWebhookRequest request);

    /**
     * Get payment status
     */
    PaymentStatusResponse getPaymentStatus(String transactionId);

    /**
     * Get payment by order
     */
    PaymentResponse getPaymentByOrder(UUID orderId);

    /**
     * Process COD payment (mark as paid on delivery)
     */
    void processCODPayment(UUID orderId);

    /**
     * Verify webhook signature
     */
    boolean verifyWebhookSignature(String signature, String data);
}

