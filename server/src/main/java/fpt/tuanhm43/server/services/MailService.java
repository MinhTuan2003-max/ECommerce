package fpt.tuanhm43.server.services;

import fpt.tuanhm43.server.entities.Order;

/**
 * Email Service Interface
 */
public interface MailService {

    /**
     * Send order confirmation email
     */
    void sendOrderConfirmation(Order order);

    /**
     * Send order status update email
     */
    void sendOrderStatusUpdate(Order order, String oldStatus, String newStatus);

    /**
     * Send payment confirmation email
     */
    void sendPaymentConfirmation(Order order);

    /**
     * Send order shipped email
     */
    void sendOrderShipped(Order order);

    /**
     * Send order delivered email
     */
    void sendOrderDelivered(Order order);

    /**
     * Send order cancelled email
     */
    void sendOrderCancelled(Order order, String reason);

    /**
     * Send password reset email
     */
    void sendPasswordResetEmail(String name, String to, String token);
}