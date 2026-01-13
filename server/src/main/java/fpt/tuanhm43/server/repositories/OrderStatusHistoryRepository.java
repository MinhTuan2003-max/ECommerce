package fpt.tuanhm43.server.repositories;

import fpt.tuanhm43.server.entities.OrderStatusHistory;
import fpt.tuanhm43.server.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order Status History Repository
 */
@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {

    /**
     * Find by order (ordered by created date)
     */
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtAsc(UUID orderId);

    /**
     * Find by status change
     */
    @Query("""
        SELECT h FROM OrderStatusHistory h 
        WHERE h.fromStatus = :fromStatus 
        AND h.toStatus = :toStatus 
        ORDER BY h.createdAt DESC
    """)
    List<OrderStatusHistory> findByStatusChange(
            @Param("fromStatus") OrderStatus fromStatus,
            @Param("toStatus") OrderStatus toStatus
    );

    /**
     * Find changes by user
     */
    List<OrderStatusHistory> findByChangedByOrderByCreatedAtDesc(String changedBy);

    /**
     * Find recent status changes
     */
    @Query("SELECT h FROM OrderStatusHistory h WHERE h.createdAt > :since ORDER BY h.createdAt DESC")
    List<OrderStatusHistory> findRecentChanges(@Param("since") LocalDateTime since);

    /**
     * Count status transitions
     */
    @Query("""
        SELECT COUNT(h) FROM OrderStatusHistory h 
        WHERE h.fromStatus = :fromStatus 
        AND h.toStatus = :toStatus
    """)
    Long countStatusTransitions(
            @Param("fromStatus") OrderStatus fromStatus,
            @Param("toStatus") OrderStatus toStatus
    );
}
