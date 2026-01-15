package fpt.tuanhm43.server.unit;

import fpt.tuanhm43.server.dtos.payment.request.SepayWebhookRequest;
import fpt.tuanhm43.server.dtos.payment.response.PaymentStatusResponse;
import fpt.tuanhm43.server.entities.Order;
import fpt.tuanhm43.server.entities.PaymentTransaction;
import fpt.tuanhm43.server.enums.PaymentMethod;
import fpt.tuanhm43.server.enums.PaymentStatus;
import fpt.tuanhm43.server.repositories.OrderRepository;
import fpt.tuanhm43.server.repositories.PaymentTransactionRepository;
import fpt.tuanhm43.server.services.InventoryService;
import fpt.tuanhm43.server.services.MailService;
import fpt.tuanhm43.server.services.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private PaymentTransactionRepository paymentTransactionRepository;
    @Mock private InventoryService inventoryService;
    @Mock private MailService mailService;

    @InjectMocks private PaymentServiceImpl paymentService;

    private UUID orderId;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        ReflectionTestUtils.setField(paymentService, "sepayWebhookKey", "test-secret-key");

        mockOrder = Order.builder()
                .id(orderId)
                .totalAmount(new BigDecimal("1000000"))
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.SEPAY)
                .build();
    }

    @Test
    @DisplayName("Khởi tạo thanh toán SePay: Phải tạo Transaction PENDING và trả về URL")
    void initiatePayment_SePay_Success() {
        // Given
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        // When
        PaymentStatusResponse response = paymentService.initiatePayment(orderId, PaymentMethod.SEPAY);

        // Then
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        assertThat(response.getPaymentUrl()).contains("transactionId=");
        verify(paymentTransactionRepository).save(any(PaymentTransaction.class));
    }

    @Test
    @DisplayName("Webhook thành công: Phải đổi trạng thái đơn hàng và TRỪ KHO THẬT")
    void handleSepayWebhook_Success() {
        PaymentServiceImpl spyPaymentService = spy(paymentService);

        String txnId = "TXN-123";
        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionId(txnId).order(mockOrder).status(PaymentStatus.PENDING).build();

        SepayWebhookRequest request = new SepayWebhookRequest();
        request.setTransactionId(txnId);
        request.setStatus("SUCCESS");
        request.setSignature("dummy-signature");

        doReturn(true).when(spyPaymentService).verifyWebhookSignature(anyString(), anyString());

        when(paymentTransactionRepository.findByTransactionId(txnId)).thenReturn(Optional.of(transaction));

        spyPaymentService.handleSepayWebhook(request);

        assertThat(transaction.getStatus()).isEqualTo(PaymentStatus.PAID);
        verify(inventoryService).deductReservedStock(orderId);
        verify(mailService).sendPaymentConfirmation(any());
    }

    @Test
    @DisplayName("Thanh toán COD: Phải trừ kho ngay khi xử lý xong")
    void processCODPayment_Success() {
        mockOrder.setPaymentMethod(PaymentMethod.COD);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        when(paymentTransactionRepository.findLatestByOrderId(orderId)).thenReturn(Optional.empty());

        when(paymentTransactionRepository.save(any(PaymentTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.processCODPayment(orderId);

        assertThat(mockOrder.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        verify(inventoryService).deductReservedStock(orderId);
        verify(paymentTransactionRepository, atLeastOnce()).save(any());
    }
}