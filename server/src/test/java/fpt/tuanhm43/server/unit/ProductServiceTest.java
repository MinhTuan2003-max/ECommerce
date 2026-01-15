package fpt.tuanhm43.server.unit;

import fpt.tuanhm43.server.dtos.PageResponseDTO;
import fpt.tuanhm43.server.dtos.product.request.ProductFilterRequest;
import fpt.tuanhm43.server.dtos.product.response.ProductResponse;
import fpt.tuanhm43.server.dtos.search.request.AdvancedSearchRequest;
import fpt.tuanhm43.server.repositories.CategoryRepository;
import fpt.tuanhm43.server.repositories.ProductRepository;
import fpt.tuanhm43.server.services.impl.ProductServiceImpl;
import fpt.tuanhm43.server.services.impl.ProductSearchServiceImpl; // Cần import cái này
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;

    @Mock private ProductSearchServiceImpl productSearchService;

    @InjectMocks private ProductServiceImpl productService;

    @Test
    @DisplayName("Khách vào web: Danh sách sản phẩm phải gọi qua Elasticsearch Search Service")
    void getAllWithFilter_ByCategory() {
        // Given
        UUID categoryId = UUID.randomUUID();
        ProductFilterRequest filter = new ProductFilterRequest();
        filter.setCategoryId(categoryId);
        filter.setPage(0);
        filter.setSize(20);

        // Tạo dữ liệu giả trả về từ ES
        ProductResponse productResponse = ProductResponse.builder()
                .name("Hoodie FPT")
                .categoryName("Hoodie")
                .build();

        PageResponseDTO<ProductResponse> mockPage = PageResponseDTO.<ProductResponse>builder()
                .content(List.of(productResponse))
                .totalElements(1L)
                .build();

        // FIX 2: Stubbing vào hàm advancedSearch thay vì Repository cũ
        when(productSearchService.advancedSearch(any(AdvancedSearchRequest.class))).thenReturn(mockPage);

        // When
        PageResponseDTO<ProductResponse> result = productService.getAllWithFilter(filter);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getCategoryName()).isEqualTo("Hoodie");

        verify(productSearchService).advancedSearch(any(AdvancedSearchRequest.class));
    }

    @Test
    @DisplayName("Khách tìm đồ: Kiểm tra luồng gọi lọc giá qua Elasticsearch")
    void getAllWithFilter_ByPriceRange() {
        // Given
        ProductFilterRequest filter = new ProductFilterRequest();
        filter.setMinPrice(BigDecimal.valueOf(1000000));
        filter.setMaxPrice(BigDecimal.valueOf(5000000));

        when(productSearchService.advancedSearch(any(AdvancedSearchRequest.class)))
                .thenReturn(PageResponseDTO.<ProductResponse>builder().content(List.of()).build());

        productService.getAllWithFilter(filter);

        verify(productSearchService).advancedSearch(any(AdvancedSearchRequest.class));
    }
}