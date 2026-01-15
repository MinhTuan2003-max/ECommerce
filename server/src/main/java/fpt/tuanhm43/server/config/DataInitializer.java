package fpt.tuanhm43.server.config;

import fpt.tuanhm43.server.entities.*;
import fpt.tuanhm43.server.repositories.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;

    @Value("${app.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin.password:Admin123!}")
    private String adminPassword;

    @PostConstruct
    @Transactional
    public void init() {
        initRolesAndAdmin();
        initCatalogData();
    }

    private void initRolesAndAdmin() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = User.builder()
                    .username(adminUsername)
                    .email("admin@hypebeast.com")
                    .password(passwordEncoder.encode(adminPassword))
                    .enabled(true)
                    .build();
            admin.getRoles().addAll(List.of(adminRole, userRole));
            userRepository.save(admin);
            log.info("Admin user created");
        }
    }

    private void initCatalogData() {
        if (categoryRepository.count() > 0) return;

        // --- CATEGORIES ---
        Category tops = createCategory("Tops", "tops");
        Category footwear = createCategory("Footwear", "footwear");
        Category accessories = createCategory("Accessories", "accessories");

        // --- PRODUCTS: TOPS (Price Range: 300k - 800k) ---
        Product tee = createProduct("Graphic Oversize Tee", "graphic-oversize-tee", tops, new BigDecimal("350000"));
        createVariantWithStock(tee, "TEE-WHT-M", "White", "M", BigDecimal.ZERO, 50);
        createVariantWithStock(tee, "TEE-WHT-L", "White", "L", BigDecimal.ZERO, 30);
        createVariantWithStock(tee, "TEE-BLK-M", "Black", "M", new BigDecimal("20000"), 20);

        Product hoodie = createProduct("Essential Hoodie", "essential-hoodie", tops, new BigDecimal("750000"));
        createVariantWithStock(hoodie, "HD-GRY-L", "Grey", "L", BigDecimal.ZERO, 15);
        createVariantWithStock(hoodie, "HD-BLK-XL", "Black", "XL", new BigDecimal("50000"), 10);

        // --- PRODUCTS: FOOTWEAR (Price Range: 1M - 5M) ---
        Product sneakers = createProduct("Street Runner V1", "street-runner-v1", footwear, new BigDecimal("2200000"));
        createVariantWithStock(sneakers, "SNK-V1-40", "Neon", "40", BigDecimal.ZERO, 12);
        createVariantWithStock(sneakers, "SNK-V1-42", "Neon", "42", BigDecimal.ZERO, 0); // Out of stock test

        Product boots = createProduct("Urban Combat Boots", "urban-boots", footwear, new BigDecimal("4800000"));
        createVariantWithStock(boots, "BT-LTH-41", "Brown", "41", BigDecimal.ZERO, 5);

        // --- PRODUCTS: ACCESSORIES (Price Range: 100k - 500k) ---
        Product cap = createProduct("Snapback Hype", "snapback-hype", accessories, new BigDecimal("250000"));
        createVariantWithStock(cap, "CAP-RED", "Red", "FreeSize", BigDecimal.ZERO, 100);

        Product socks = createProduct("Logo Crew Socks", "logo-crew-socks", accessories, new BigDecimal("120000"));
        createVariantWithStock(socks, "SOCK-WHT", "White", "FreeSize", BigDecimal.ZERO, 200);

        log.info("ZZCatalog data seeded with multiple categories and price points");
    }

    private Category createCategory(String name, String slug) {
        return categoryRepository.save(Category.builder().name(name).slug(slug).isActive(true).build());
    }

    private Product createProduct(String name, String slug, Category category, BigDecimal basePrice) {
        return productRepository.save(Product.builder()
                .name(name).slug(slug).category(category).basePrice(basePrice).isActive(true).build());
    }

    private void createVariantWithStock(Product product, String sku, String color, String size, BigDecimal adj, int qty) {
        ProductVariant variant = variantRepository.save(ProductVariant.builder()
                .product(product).sku(sku).color(color).size(size).priceAdjustment(adj).isActive(true).build());

        inventoryRepository.save(Inventory.builder()
                .productVariant(variant).quantityAvailable(qty).quantityReserved(0).lowStockThreshold(5).build());
    }
}