package fpt.tuanhm43.server.unit;

import fpt.tuanhm43.server.dtos.product.request.CreateProductVariantRequest;
import fpt.tuanhm43.server.dtos.product.response.ProductVariantResponse;
import fpt.tuanhm43.server.entities.*;
import fpt.tuanhm43.server.exceptions.BadRequestException;
import fpt.tuanhm43.server.mappers.ProductVariantMapper;
import fpt.tuanhm43.server.repositories.ProductRepository;
import fpt.tuanhm43.server.repositories.ProductVariantRepository;
import fpt.tuanhm43.server.services.ProductSearchService;
import fpt.tuanhm43.server.services.impl.ProductVariantServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductVariantServiceTest {

    @Mock private ProductVariantRepository variantRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductVariantMapper variantMapper;

    @Mock private ProductSearchService productSearchService;

    @InjectMocks private ProductVariantServiceImpl variantService;

    @Test
    @DisplayName("Quản lý: Tạo biến thể mới (Áo Thun Rồng - Size L - Màu Đen) phải gán đúng SKU và kho")
    void createVariant_Success() {

        UUID productId = UUID.randomUUID();
        CreateProductVariantRequest request = new CreateProductVariantRequest();
        request.setSku("AT-RONG-L-BLACK");
        request.setSize("L");
        request.setColor("Black");

        Product product = Product.builder().id(productId).name("Áo Thun Rồng").build();

        when(variantRepository.existsBySku(request.getSku())).thenReturn(false);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(variantRepository.save(any(ProductVariant.class))).thenAnswer(i -> i.getArguments()[0]);

        when(variantMapper.toResponse(any(ProductVariant.class))).thenReturn(ProductVariantResponse.builder().build());

        variantService.createVariant(productId, request);

        verify(variantRepository).save(argThat(v -> {
            assertThat(v.getSku()).isEqualTo("AT-RONG-L-BLACK");
            assertThat(v.getInventory().getQuantityAvailable()).isZero();
            return true;
        }));

        verify(productSearchService).syncToElasticsearch(productId);
    }

    @Test
    @DisplayName("Quản lý: Không được phép tạo trùng SKU")
    void createVariant_DuplicateSku() {
        String duplicateSku = "EXISTING-SKU";
        CreateProductVariantRequest request = new CreateProductVariantRequest();
        request.setSku(duplicateSku);

        when(variantRepository.existsBySku(duplicateSku)).thenReturn(true);

        assertThatThrownBy(() -> variantService.createVariant(UUID.randomUUID(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }
}