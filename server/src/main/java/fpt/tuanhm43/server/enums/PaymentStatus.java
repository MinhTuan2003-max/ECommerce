package fpt.tuanhm43.server.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("Chờ thanh toán"),
    PROCESSING("Đang xử lý"),
    PAID("Đã thanh toán"),
    FAILED("Thanh toán thất bại"),
    REFUNDED("Đã hoàn tiền");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean isFinal() {
        return this == PAID || this == FAILED || this == REFUNDED;
    }
}
