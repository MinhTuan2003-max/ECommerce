package fpt.tuanhm43.server.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    COD("Thanh toán khi nhận hàng"),
    SEPAY("Chuyển khoản ngân hàng (SePay)"),
    MOMO("Ví MoMo"),
    ZALOPAY("Ví ZaloPay");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public boolean requiresOnlinePayment() {
        return this != COD;
    }
}
