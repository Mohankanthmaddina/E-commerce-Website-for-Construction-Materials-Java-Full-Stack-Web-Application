package com.example.buildpro.config;

import com.example.buildpro.model.Category;
import com.example.buildpro.model.Product;
import com.example.buildpro.model.User;
import com.example.buildpro.repository.CategoryRepository;
import com.example.buildpro.repository.ProductRepository;
import com.example.buildpro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only load data if no categories exist
        if (categoryRepository.count() == 0) {
            loadSampleData();
        }

        // Load sample users if no users exist
        if (userRepository.count() == 0) {
            loadSampleUsers();
        }
    }

    private void loadSampleData() {
        System.out.println("Loading sample data...");

        // Create categories
        Category tools = new Category();
        tools.setName("Tools");
        tools = categoryRepository.save(tools);

        Category materials = new Category();
        materials.setName("Materials");
        materials = categoryRepository.save(materials);

        Category hardware = new Category();
        hardware.setName("Hardware");
        hardware = categoryRepository.save(hardware);

        // Create sample products
        Product hammer = new Product();
        hammer.setName("Heavy Duty Hammer");
        hammer.setBrand("BuildMaster");
        hammer.setDescription("Professional grade hammer for construction work");
        hammer.setPrice(25.99);
        hammer.setStockQuantity(50);
        hammer.setImageUrl("https://images.unsplash.com/photo-1581094794329-c8112a89af12?w=400");
        hammer.setSpecifications("{\"weight\":\"2.5lbs\",\"handle\":\"Fiberglass\",\"head\":\"Steel\"}");
        hammer.setCategory(tools);
        productRepository.save(hammer);

        Product cement = new Product();
        cement.setName("Portland Cement 50kg");
        cement.setBrand("CementPro");
        cement.setDescription("High quality Portland cement for construction");
        cement.setPrice(12.50);
        cement.setStockQuantity(100);
        cement.setImageUrl("https://images.unsplash.com/photo-1581094794329-c8112a89af12?w=400");
        cement.setSpecifications("{\"weight\":\"50kg\",\"type\":\"Portland\",\"grade\":\"53\"}");
        cement.setCategory(materials);
        productRepository.save(cement);

        Product screws = new Product();
        screws.setName("Stainless Steel Screws");
        screws.setBrand("FastenRight");
        screws.setDescription("Corrosion resistant stainless steel screws");
        screws.setPrice(8.99);
        screws.setStockQuantity(200);
        screws.setImageUrl("https://images.unsplash.com/photo-1581094794329-c8112a89af12?w=400");
        screws.setSpecifications("{\"material\":\"Stainless Steel\",\"length\":\"2 inches\",\"count\":\"100 pieces\"}");
        screws.setCategory(hardware);
        productRepository.save(screws);

        System.out.println("Sample data loaded successfully!");
        System.out.println(
                "Created " + categoryRepository.count() + " categories and " + productRepository.count() + " products");
    }

    private void loadSampleUsers() {
        System.out.println("Loading sample users...");

        // Create admin user
        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@buildpro.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(User.Role.ADMIN);
        admin.setIsVerified(true);
        admin.setCreatedAt(java.time.LocalDateTime.now());
        admin.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(admin);

        // Create regular users
        User user1 = new User();
        user1.setName("John Smith");
        user1.setEmail("john@example.com");
        user1.setPassword(passwordEncoder.encode("password123"));
        user1.setRole(User.Role.USER);
        user1.setIsVerified(true);
        user1.setCreatedAt(java.time.LocalDateTime.now().minusDays(5));
        user1.setUpdatedAt(java.time.LocalDateTime.now().minusDays(1));
        userRepository.save(user1);

        User user2 = new User();
        user2.setName("Sarah Johnson");
        user2.setEmail("sarah@example.com");
        user2.setPassword(passwordEncoder.encode("password123"));
        user2.setRole(User.Role.USER);
        user2.setIsVerified(false);
        user2.setCreatedAt(java.time.LocalDateTime.now().minusDays(3));
        user2.setUpdatedAt(java.time.LocalDateTime.now().minusDays(2));
        userRepository.save(user2);

        User user3 = new User();
        user3.setName("Mike Wilson");
        user3.setEmail("mike@example.com");
        user3.setPassword(passwordEncoder.encode("password123"));
        user3.setRole(User.Role.USER);
        user3.setIsVerified(true);
        user3.setCreatedAt(java.time.LocalDateTime.now().minusDays(10));
        user3.setUpdatedAt(java.time.LocalDateTime.now().minusDays(5));
        userRepository.save(user3);

        User user4 = new User();
        user4.setName("Emily Davis");
        user4.setEmail("emily@example.com");
        user4.setPassword(passwordEncoder.encode("password123"));
        user4.setRole(User.Role.USER);
        user4.setIsVerified(false);
        user4.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));
        user4.setUpdatedAt(java.time.LocalDateTime.now().minusHours(2));
        userRepository.save(user4);

        System.out.println("Sample users loaded successfully!");
        System.out.println("Created " + userRepository.count() + " users");
    }
}
