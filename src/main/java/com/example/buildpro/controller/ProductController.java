package com.example.buildpro.controller;

import com.example.buildpro.model.Product;
import com.example.buildpro.dto.ProductDTO;
import com.example.buildpro.model.Category;
import com.example.buildpro.service.CartService;
import com.example.buildpro.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.example.buildpro.model.User;
import com.example.buildpro.service.UserService;
import java.util.Map;

@Controller
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;
    
    @GetMapping("/view")
    public String productsPage(Model model, Principal principal) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        if (principal != null) {
            String email = principal.getName(); // Spring Security provides this
            model.addAttribute("currentUserEmail", email);

            Optional<User> userOpt = userService.findByEmail(email);
            int cartSize = userOpt.map(u -> cartService.getCartByUser(u).getCartItems().size()).orElse(0);
            model.addAttribute("cartSize", cartSize);
        }

        return "products";
    }


    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
   @GetMapping("/{id}/view")
    public String productDetailPage(@PathVariable Long id, Model model) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "product-detail";  // this will render product-detail.html
        } else {
            return "error/404"; // make a simple error page or redirect
        }
    }
    
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String categoryName) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryName));
    }
    
    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<Product>> getProductsByBrand(@PathVariable String brand) {
        return ResponseEntity.ok(productService.getProductsByBrand(brand));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        return ResponseEntity.ok(productService.searchProducts(q));
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }   
}

