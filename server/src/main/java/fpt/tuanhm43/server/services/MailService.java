package fpt.tuanhm43.server.services;

import fpt.tuanhm43.server.entities.Order;

public interface MailService {
    void sendOrderConfirmation(Order order);
    void sendOrderShipped(Order order);
    void sendOrderDelivered(Order order);
    void sendOrderCancelled(Order order, String reason);
}