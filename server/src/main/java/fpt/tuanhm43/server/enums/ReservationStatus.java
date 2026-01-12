package fpt.tuanhm43.server.enums;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    ACTIVE("Đang giữ hàng"),
    COMPLETED("Đã hoàn tất"),
    EXPIRED("Đã hết hạn"),
    CANCELLED("Đã hủy");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean canRelease() {
        return this == EXPIRED || this == CANCELLED;
    }
}
