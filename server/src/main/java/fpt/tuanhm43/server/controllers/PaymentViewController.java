package fpt.tuanhm43.server.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class PaymentViewController {

    private static final String MY_BANK_ACCOUNT = "0335666888"; // Thay số của bạn
    private static final String MY_BANK_NAME = "MBBank";        // Thay ngân hàng của bạn

    @GetMapping("/payment-demo/sepay")
    @ResponseBody
    public String viewPaymentQr(@RequestParam String transactionId,
                                @RequestParam BigDecimal amount,
                                @RequestParam String orderId) {

        long amountLong = amount.longValue();

        String qrUrl = String.format("https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%d&des=%s",
                MY_BANK_ACCOUNT,
                URLEncoder.encode(MY_BANK_NAME, StandardCharsets.UTF_8),
                amountLong,
                URLEncoder.encode(transactionId, StandardCharsets.UTF_8));

        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Thanh toán SePay - Đơn hàng %s</title>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; background-color: #f4f7f6; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }
                    .payment-card { background: white; width: 100%%; max-width: 420px; padding: 30px; border-radius: 16px; box-shadow: 0 10px 25px rgba(0,0,0,0.05); text-align: center; }
                    .logo { font-weight: bold; font-size: 24px; color: #007aff; margin-bottom: 10px; display: block; }
                    .order-info { background: #f8f9fa; padding: 15px; border-radius: 8px; margin-bottom: 20px; font-size: 0.95rem; color: #555; }
                    .qr-container { padding: 10px; border: 1px solid #eee; border-radius: 12px; margin-bottom: 15px; }
                    .qr-img { width: 100%%; max-width: 280px; display: block; margin: 0 auto; }
                    .amount-text { font-size: 28px; font-weight: 800; color: #e63946; margin: 10px 0; }
                    .dev-tools { margin-top: 30px; padding-top: 20px; border-top: 2px dashed #eee; }
                    .btn-simulate { background-color: #10b981; color: white; border: none; padding: 12px 24px; font-size: 16px; font-weight: 600; border-radius: 8px; cursor: pointer; width: 100%%; }
                    .btn-simulate:hover { background-color: #059669; }
                </style>
            </head>
            <body>
                <div class="payment-card">
                    <span class="logo">SEPAY PAYMENT</span>

                    <div class="order-info">
                        Đơn hàng: <strong>%s</strong><br>
                        Mã giao dịch: <strong>%s</strong>
                    </div>

                    <div class="amount-text">%,d VND</div>

                    <div class="qr-container">
                        <img src="%s" class="qr-img" alt="Quét mã để thanh toán">
                    </div>

                    <div class="dev-tools">
                        <p style="font-size: 0.8rem; color: #888; margin-bottom: 10px;">(Demo Mode)</p>
                        <button class="btn-simulate" onclick="simulateSuccess()">
                            Giả lập thanh toán thành công
                        </button>
                    </div>
                </div>

                <script>
                    function simulateSuccess() {
                        const transactionId = '%s';
                        const orderId = '%s'; // CẦN TRUYỀN CÁI NÀY VÀO JS

                        fetch('/api/v1/payments/webhook/sepay', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({
                                transactionId: transactionId,
                                orderId: orderId, // THÊM DÒNG NÀY ĐỂ KHỚP DTO
                                status: 'SUCCESS',
                                signature: 'dev-bypass',
                                data: { message: 'Paid via Simulator' }
                            })
                        })
                        .then(response => {
                            if (response.ok) {
                                alert('Thành công! Webhook đã kích hoạt.');
                            } else {
                                alert('Lỗi: ' + response.status);
                            }
                        })
                        .catch(err => alert('Lỗi kết nối: ' + err));
                    }

                    setInterval(() => {
                        fetch('/api/v1/payments/status/' + '%s')
                            .then(res => res.json())
                            .then(data => {
                                if (data.data.status === 'PAID') {
                                    alert('Giao dịch hoàn tất! Cảm ơn bạn.');
                                    // window.location.href = '/thank-you'; // Chuyển trang nếu muốn
                                }
                            });
                    }, 3000);
                </script>
            </body>
            </html>
        """.formatted(
                orderId,
                orderId, transactionId,
                amountLong,
                qrUrl,
                transactionId, orderId,
                transactionId
        );
    }
}