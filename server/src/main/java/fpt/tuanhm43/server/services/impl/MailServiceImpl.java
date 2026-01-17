package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.entities.Order;
import fpt.tuanhm43.server.services.MailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private static final String PARAM_CUSTOMER_NAME = "customerName";
    private static final String PARAM_ORDER_NUMBER = "orderNumber";
    private static final String PARAM_TRACKING_URL = "trackingUrl";
    private static final String PARAM_TOTAL_AMOUNT = "totalAmount";
    private static final String PARAM_SHIPPING_ADDR = "shippingAddress";
    private static final String PARAM_CANCEL_REASON = "cancelReason";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override @Async
    public void sendOrderConfirmation(Order order) {
        Map<String, Object> props = new HashMap<>();
        props.put(PARAM_CUSTOMER_NAME, order.getCustomerName());
        props.put(PARAM_ORDER_NUMBER, order.getOrderNumber());
        props.put(PARAM_TOTAL_AMOUNT, order.getTotalAmount());
        props.put(PARAM_SHIPPING_ADDR, order.getShippingAddress());
        props.put(PARAM_TRACKING_URL, buildTrackingUrl(order));

        sendHtmlEmail(order.getCustomerEmail(), "Xác nhận đơn hàng #" + order.getOrderNumber(), "order-confirmation", props);
    }

    @Override @Async
    public void sendOrderShipped(Order order) {
        Map<String, Object> props = new HashMap<>();
        props.put(PARAM_CUSTOMER_NAME, order.getCustomerName());
        props.put(PARAM_ORDER_NUMBER, order.getOrderNumber());
        props.put(PARAM_TRACKING_URL, buildTrackingUrl(order));

        sendHtmlEmail(order.getCustomerEmail(), "Đơn hàng đang giao #" + order.getOrderNumber(), "order-shipped", props);
    }

    @Override @Async
    public void sendOrderDelivered(Order order) {
        Map<String, Object> props = new HashMap<>();
        props.put(PARAM_CUSTOMER_NAME, order.getCustomerName());
        props.put(PARAM_ORDER_NUMBER, order.getOrderNumber());

        sendHtmlEmail(order.getCustomerEmail(), "Giao hàng thành công #" + order.getOrderNumber(), "order-delivered", props);
    }

    @Override @Async
    public void sendOrderCancelled(Order order, String reason) {
        Map<String, Object> props = new HashMap<>();
        props.put(PARAM_CUSTOMER_NAME, order.getCustomerName());
        props.put(PARAM_ORDER_NUMBER, order.getOrderNumber());
        props.put(PARAM_CANCEL_REASON, reason);

        sendHtmlEmail(order.getCustomerEmail(), "Thông báo hủy đơn hàng #" + order.getOrderNumber(), "order-cancelled", props);
    }

    @Override @Async
    public void sendPaymentConfirmation(Order order) {
        Map<String, Object> props = new HashMap<>();
        props.put(PARAM_CUSTOMER_NAME, order.getCustomerName());
        props.put(PARAM_ORDER_NUMBER, order.getOrderNumber());
        props.put(PARAM_TOTAL_AMOUNT, order.getTotalAmount());

        props.put("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "Chuyển khoản");
        props.put("paymentTime", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")));
        props.put(PARAM_TRACKING_URL, buildTrackingUrl(order));

        sendHtmlEmail(order.getCustomerEmail(),
                "HUNG HYPEBEAST - Xác nhận thanh toán đơn hàng #" + order.getOrderNumber(),
                "payment-confirmation",
                props);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String name, String to, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        Map<String, Object> props = new HashMap<>();
        props.put(PARAM_CUSTOMER_NAME, name);
        props.put("resetUrl", resetUrl);
        props.put("expiryTime", "15 minutes");

        sendHtmlEmail(to, "Reset Your Password - HUNG HYPEBEAST", "password-reset", props);
    }

    private String buildTrackingUrl(Order order) {
        return frontendUrl + "/track/" + order.getTrackingToken();
    }

    private void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> properties) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            Context context = new Context();
            context.setVariables(properties);
            String html = templateEngine.process(templateName, context);

            helper.setFrom(fromName + " <" + fromEmail + ">");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Email sent to {} with template {}", to, templateName);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}