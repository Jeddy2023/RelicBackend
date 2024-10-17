package com.example.relicv2.util;

import com.example.relicv2.enums.Condition;
import com.example.relicv2.enums.Role;
import com.example.relicv2.model.Category;
import com.example.relicv2.model.Product;
import com.example.relicv2.model.ShippingAddress;
import com.example.relicv2.model.User;
import com.example.relicv2.repository.CategoryRepository;
import com.example.relicv2.repository.ProductRepository;
import com.example.relicv2.repository.ShippingAddressRepository;
import com.example.relicv2.repository.UserRepository;
import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Order(2)
public class ProductSeeder implements CommandLineRunner {
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final Logger logger = LoggerFactory.getLogger(ProductSeeder.class);
    private final Faker faker = new Faker();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ProductSeeder(ProductRepository productRepository, PasswordEncoder passwordEncoder, CategoryRepository categoryRepository, UserRepository userRepository, ShippingAddressRepository shippingAddressRepository) {
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.shippingAddressRepository = shippingAddressRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Seeding products...");

        if (productRepository.count() > 0) {
            logger.info("Products already exist in database. Skipping seeding...");
            return;
        }

        List<Category> categories = categoryRepository.findAll();

        // create vendor
        User vendor = new User();
        vendor.setEmail("vendor@gmail.com");
        vendor.setUsername("vendor");
        vendor.setShopname("VendorStores");
        vendor.setPassword(passwordEncoder.encode("@Password123"));
        vendor.setFirstname("Jane");
        vendor.setPhonenumber("09046807203");
        vendor.setLastname("Doe");
        vendor.setRole(Role.SELLER);
        vendor.setVerified(true);
        vendor.setBlocked(false);
        userRepository.save(vendor);

        // Seed shipping address for the vendor
        ShippingAddress vendorAddress = new ShippingAddress();
        vendorAddress.setStreetAddress(faker.address().streetAddress());
        vendorAddress.setCity(faker.address().city());
        vendorAddress.setState(faker.address().state());
        vendorAddress.setPostalCode(faker.address().zipCode());
        vendorAddress.setUser(vendor);
        shippingAddressRepository.save(vendorAddress);

        Date currentDate = new Date();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            Long price = (long) (new Random().nextInt(10000) + 5000);

            // Randomly generate a bidding time limit between 5 months to 1 year
            int[] possibleMonths = {5, 6, 8, 12};
            int selectedMonths = possibleMonths[random.nextInt(possibleMonths.length)];

            // Calculate the bidding time limit (selectedMonths from the current date)
            Date endDate = new Date(currentDate.getTime() + TimeUnit.DAYS.toMillis(selectedMonths * 30L));
            String formattedBiddingTimeLimit = dateFormat.format(endDate);

            // create a new product
            Product product = new Product();
            product.setName(faker.commerce().productName());
            product.setShortDescription(faker.lorem().sentence());
            product.setDescription(faker.lorem().paragraph());
            product.setVendor(vendor);
            product.setMinimumBid(price);
            product.setCurrentBid(price);
            product.setProductImageUrl("https://res.cloudinary.com/dyktnfgye/image/upload/v1722173585/8_hyx9vy.png");
            product.setCategory(categories.get(i % categories.size()));
            product.setCondition(new Random().nextInt(2) == 0 ? Condition.NEW : Condition.USED);
            product.setBiddingTimeLimit(formattedBiddingTimeLimit);
            product.setCreatedAt(new Date());
            product.setUpdatedAt(new Date());
            product.setBiddingClosed(false);
            product.setBlocked(false);
            List<String> imageUrls = Arrays.asList(
                    "https://res.cloudinary.com/dyktnfgye/image/upload/v1722173585/8_hyx9vy.png",
                    "https://res.cloudinary.com/dyktnfgye/image/upload/v1722173585/8_hyx9vy.png",
                    "https://res.cloudinary.com/dyktnfgye/image/upload/v1722173585/8_hyx9vy.png"
            );
            product.setImageUrls(imageUrls);
            productRepository.save(product);
        }

        logger.info("Products seeded successfully");
    }
}
