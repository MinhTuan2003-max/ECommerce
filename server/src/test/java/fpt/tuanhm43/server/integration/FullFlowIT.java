package fpt.tuanhm43.server.integration;

import com.jayway.jsonpath.JsonPath;
import fpt.tuanhm43.server.entities.*;
import fpt.tuanhm43.server.repositories.*;
import fpt.tuanhm43.server.services.MailService;
import fpt.tuanhm43.server.services.impl.PaymentServiceImpl;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FullFlowIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager entityManager;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductSearchRepository productSearchRepository;

    @SpyBean private PaymentServiceImpl paymentService;
    @MockBean private MailService mailService;

    private UUID variantId;
    private MockHttpSession session;

    @BeforeEach
    @Transactional
    void setup() {
        session = new MockHttpSession();

        Category category = categoryRepository.save(Category.builder()
                .name("Fashion").slug("fashion-" + UUID.randomUUID()).isActive(true).build());

        Product product = productRepository.save(Product.builder()
                .name("Hypebeast Tee").slug("tee-" + UUID.randomUUID())
                .category(category).basePrice(new BigDecimal("500000")).isActive(true).build());

        String validSku = ("SKU-" + UUID.randomUUID().toString().substring(0, 8)).toUpperCase();

        ProductVariant variant = variantRepository.save(ProductVariant.builder()
                .product(product).sku(validSku).priceAdjustment(BigDecimal.ZERO).isActive(true).build());

        variantId = variant.getId();

        inventoryRepository.save(Inventory.builder()
                .productVariant(variant).quantityAvailable(10).quantityReserved(0).build());

        entityManager.flush();

        productSearchRepository.save(fpt.tuanhm43.server.documents.ProductSearchDocument.builder()
                .id(product.getId().toString())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .minPrice(product.getMinPrice().doubleValue())
                .categoryId(category.getId().toString())
                .categoryName(category.getName())
                .isActive(true)
                .createdAt(java.time.LocalDateTime.now())
                .build());

        entityManager.clear();
    }

    @Test
    @DisplayName("Main E-Commerce Flow Integration Test")
    @Transactional
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testFullWorkflow() throws Exception {

        // STEP 1: Catalog
        String searchJson = """
            {
                "page": 0,
                "size": 10,
                "keyword": "Hypebeast",
                "fuzzy": false
            }
            """;

        mockMvc.perform(post("/api/v1/products/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Hypebeast Tee"));

        // STEP 2: Add to Cart
        String addCartJson = "{\"variantId\": \"" + variantId + "\", \"quantity\": 1}";
        mockMvc.perform(post("/api/v1/cart/add")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addCartJson))
                .andExpect(status().isCreated());

        // STEP 3: Checkout
        String checkoutJson = """
            {
                "customerName": "Tuan HM",
                "customerEmail": "tuan@fpt.edu.vn",
                "customerPhone": "0988888888",
                "shippingAddress": "Hanoi, Vietnam",
                "paymentMethod": "SEPAY"
            }
            """;

        MvcResult orderResult = mockMvc.perform(post("/api/v1/orders/from-cart")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutJson))
                .andExpect(status().isCreated())
                .andReturn();

        String orderBody = orderResult.getResponse().getContentAsString();
        UUID orderId = UUID.fromString(JsonPath.read(orderBody, "$.data.id"));

        // STEP 4: Initiate Payment
        MvcResult payInitResult = mockMvc.perform(post("/api/v1/payments/" + orderId + "/initiate")
                        .param("method", "SEPAY")
                        .session(session))
                .andExpect(status().isCreated())
                .andReturn();

        String realTxnId = JsonPath.read(payInitResult.getResponse().getContentAsString(), "$.data.transactionId");

        // STEP 5: Webhook
        doReturn(true).when(paymentService).verifyWebhookSignature(anyString(), anyString());
        String webhookJson = "{\"transactionId\": \"%s\", \"orderId\": \"%s\", \"status\": \"SUCCESS\", \"signature\": \"valid\"}"
                .formatted(realTxnId, orderId);

        mockMvc.perform(post("/api/v1/payments/webhook/sepay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookJson))
                .andExpect(status().isOk());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        // STEP 6: Verify Inventory
        Inventory freshInv = inventoryRepository.findByProductVariantId(variantId).orElseThrow();
        assertThat(freshInv.getQuantityAvailable()).isEqualTo(9);
        assertThat(freshInv.getQuantityReserved()).isZero();

        System.out.println("ALL STEPS PASSED!");
    }

    @AfterEach
    void tearDown() {
        productSearchRepository.deleteAll();

        log.info("Cleaned up Elasticsearch and Database state.");
    }
}