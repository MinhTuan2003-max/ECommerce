package fpt.tuanhm43.server.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Đã xác nhận"),
    PAID("Đã thanh toán"),
    SHIPPING("Đang giao hàng"),
    DELIVERED("Đã giao hàng"),
    CANCELLED("Đã hủy");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED -> newStatus == PAID || newStatus == CANCELLED;
            case PAID -> newStatus == SHIPPING || newStatus == CANCELLED;
            case SHIPPING -> newStatus == DELIVERED || newStatus == CANCELLED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
