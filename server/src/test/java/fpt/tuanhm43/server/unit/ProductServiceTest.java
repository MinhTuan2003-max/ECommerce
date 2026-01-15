package fpt.tuanhm43.server.unit;

import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.product.request.ProductFilterRequest;
import fpt.tuanhm43.server.dtos.product.response.ProductResponse;
import fpt.tuanhm43.server.entities.*;
import fpt.tuanhm43.server.repositories.CategoryRepository;
import fpt.tuanhm43.server.repositories.ProductRepository;
import fpt.tuanhm43.server.services.impl.ProductServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private ProductServiceImpl productService;

    @Test
    @DisplayName("Khách vào web: Danh sách sản phẩm phải có phân trang và lọc được theo Category")
    void getAllWithFilter_ByCategory() {
        // Given: Giả lập yêu cầu lọc Hoodie, trang 0, size 20
        UUID categoryId = UUID.randomUUID();
        ProductFilterRequest filter = new ProductFilterRequest();
        filter.setCategoryId(categoryId);
        filter.setPage(0);
        filter.setSize(20);

        Category category = Category.builder().id(categoryId).name("Hoodie").build();
        Product product = Product.builder().name("Hoodie FPT").category(category).isActive(true).variants(List.of()).build();
        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.findByCategoryId(eq(categoryId), any(Pageable.class))).thenReturn(page);

        // When
        PageResponseDTO<ProductResponse> result = productService.getAllWithFilter(filter);

        // Then: Kiểm tra kết quả trả về có đúng Category không
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategoryName()).isEqualTo("Hoodie");
        verify(productRepository).findByCategoryId(eq(categoryId), any(Pageable.class));
    }

    @Test
    @DisplayName("Khách tìm đồ: Phải lọc được theo khoảng giá (Ví dụ: 1tr - 5tr)")
    void getAllWithFilter_ByPriceRange() {
        // Given
        ProductFilterRequest filter = new ProductFilterRequest();
        filter.setMinPrice(new BigDecimal("1000000"));
        filter.setMaxPrice(new BigDecimal("5000000"));


        when(productRepository.findByPriceRange(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        // When
        productService.getAllWithFilter(filter);

        // Then: Đảm bảo Repository được gọi với đúng giá trị BigDecimal
        verify(productRepository).findByPriceRange(
                argThat(price -> price.compareTo(BigDecimal.valueOf(1000000.0)) == 0),
                argThat(price -> price.compareTo(BigDecimal.valueOf(5000000.0)) == 0),
                any(Pageable.class)
        );
    }
}