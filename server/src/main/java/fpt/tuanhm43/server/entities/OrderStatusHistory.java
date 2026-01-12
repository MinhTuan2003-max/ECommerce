package fpt.tuanhm43.server.entities;

import fpt.tuanhm43.server.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "order_status_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus toStatus;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private String changedBy;
}
