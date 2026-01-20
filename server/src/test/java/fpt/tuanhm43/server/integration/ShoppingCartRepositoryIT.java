package fpt.tuanhm43.server.integration;

import fpt.tuanhm43.server.entities.ShoppingCart;
import fpt.tuanhm43.server.repositories.ShoppingCartRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ShoppingCartRepositoryIT {

    @Autowired
    private ShoppingCartRepository cartRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should delete only carts that have expired before the given time")
    void deleteExpiredCarts_ShouldDeleteCorrectData() {
        System.out.println("========== BẮT ĐẦU TEST: XÓA CART HẾT HẠN ==========");

        // --- GIVEN (Chuẩn bị dữ liệu) ---
        LocalDateTime now = LocalDateTime.now();
        System.out.println("-> Thời gian mốc (NOW): " + now);

        // Cart 1: Đã hết hạn (cách đây 1 tiếng) -> CẦN XÓA
        ShoppingCart expiredCart1 = new ShoppingCart();
        expiredCart1.setExpiresAt(now.minusHours(1));
        ShoppingCart savedCart1 = entityManager.persist(expiredCart1);
        System.out.println("-> [Tạo] Cart 1 (Hết hạn 1h trước) - ID: " + savedCart1.getId());

        // Cart 2: Đã hết hạn (cách đây 1 phút) -> CẦN XÓA
        ShoppingCart expiredCart2 = new ShoppingCart();
        expiredCart2.setExpiresAt(now.minusMinutes(1));
        ShoppingCart savedCart2 = entityManager.persist(expiredCart2);
        System.out.println("-> [Tạo] Cart 2 (Hết hạn 1p trước) - ID: " + savedCart2.getId());

        // Cart 3: Chưa hết hạn (còn 1 tiếng nữa) -> GIỮ LẠI
        ShoppingCart activeCart = new ShoppingCart();
        activeCart.setExpiresAt(now.plusHours(1));
        ShoppingCart savedCart3 = entityManager.persist(activeCart);
        System.out.println("-> [Tạo] Cart 3 (Còn hạn 1h nữa)   - ID: " + savedCart3.getId());

        // Đẩy dữ liệu xuống DB H2
        entityManager.flush();

        // In ra danh sách hiện có trong DB trước khi xóa
        long countBefore = cartRepository.count();
        System.out.println("\n--- TRƯỚC KHI CHẠY DELETE ---");
        System.out.println("-> Tổng số cart trong DB: " + countBefore);
        cartRepository.findAll().forEach(c ->
                System.out.println("   + Cart ID: " + c.getId() + " | ExpiresAt: " + c.getExpiresAt())
        );

        // --- WHEN (Thực thi hành động) ---
        System.out.println("\n--- ĐANG CHẠY LỆNH DELETE ---");
        int deletedCount = cartRepository.deleteExpiredCarts(now);
        System.out.println("-> Số lượng bản ghi đã xóa: " + deletedCount);

        // --- THEN (Kiểm tra kết quả) ---
        System.out.println("\n--- SAU KHI CHẠY DELETE ---");

        // In ra danh sách còn lại trong DB
        long countAfter = cartRepository.count();
        System.out.println("-> Tổng số cart còn lại: " + countAfter);
        cartRepository.findAll().forEach(c ->
                System.out.println("   + Cart ID: " + c.getId() + " | ExpiresAt: " + c.getExpiresAt())
        );

        // Assertions (Code kiểm tra logic)
        assertThat(deletedCount).isEqualTo(2);

        // Kiểm tra Cart 1 và 2 đã mất
        boolean isCart1Exist = cartRepository.findById(savedCart1.getId()).isPresent();
        boolean isCart2Exist = cartRepository.findById(savedCart2.getId()).isPresent();

        System.out.println("\n-> Kiểm tra Cart 1 (Nên xóa): " + (isCart1Exist ? "CÒN (Sai)" : "ĐÃ MẤT (Đúng)"));
        System.out.println("-> Kiểm tra Cart 2 (Nên xóa): " + (isCart2Exist ? "CÒN (Sai)" : "ĐÃ MẤT (Đúng)"));

        assertThat(isCart1Exist).isFalse();
        assertThat(isCart2Exist).isFalse();

        // Kiểm tra Cart 3 phải vẫn còn sống
        Optional<ShoppingCart> remainingCart = cartRepository.findById(savedCart3.getId());
        boolean isCart3Exist = remainingCart.isPresent();
        System.out.println("-> Kiểm tra Cart 3 (Nên còn): " + (isCart3Exist ? "VẪN CÒN (Đúng)" : "ĐÃ MẤT (Sai)"));

        assertThat(remainingCart).isPresent();
        assertThat(remainingCart.get().getId()).isEqualTo(activeCart.getId());

        System.out.println("========== KẾT THÚC TEST ==========");
    }
}