package com.example.buildpro.service;


import com.example.buildpro.model.Product;
import com.example.buildpro.dto.ProductDTO;
import com.example.buildpro.model.Category;
import com.example.buildpro.repository.ProductRepository;
import com.example.buildpro.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    // New method: convert Product → ProductDTO
    public List<ProductDTO> getAllProductsDTO() {
        return productRepository.findAll().stream().map(product -> {
            ProductDTO dto = new ProductDTO();
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setBrand(product.getBrand());
            dto.setDescription(product.getDescription());
            dto.setPrice(product.getPrice());
            dto.setStockQuantity(product.getStockQuantity());
            dto.setImageUrl(product.getImageUrl());
            dto.setSpecifications(product.getSpecifications());
            dto.setCategoryName(product.getCategory().getName()); // map category
            return dto;
        }).toList();
    }
    
    @GetMapping("/")
    public String productsPage() {
        return "/products/view"; 
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    public List<Product> getProductsByCategory(String categoryName) {
        return productRepository.findByCategoryName(categoryName);
    }
    
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }
    
    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCase(query);
    }
    
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    public Product updateProduct(Long id, Product productDetails) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setName(productDetails.getName());
            product.setBrand(productDetails.getBrand());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setStockQuantity(productDetails.getStockQuantity());
            product.setImageUrl(productDetails.getImageUrl());
            product.setSpecifications(productDetails.getSpecifications());
            return productRepository.save(product);
        }
        return null;
    }
    
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }
    /*
    public Object saveCategory(Category category) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveCategory'");
    }*/

    public void saveProduct(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setBrand(dto.getBrand());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setDescription(dto.getDescription());
        product.setSpecifications(dto.getSpecifications());
        product.setImageUrl(dto.getImageUrl()); // Add imageUrl mapping
        
        Category category = categoryRepository.findById(dto.getCategoryId())
                               .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);

        productRepository.save(product);
    }

    public ProductDTO getProductDTOById(Long id) {
        try {
            return productRepository.findById(id).map(product -> {
                ProductDTO dto = new ProductDTO();
                dto.setId(product.getId());
                dto.setName(product.getName());
                dto.setBrand(product.getBrand());
                dto.setDescription(product.getDescription());
                dto.setPrice(product.getPrice());
                dto.setStockQuantity(product.getStockQuantity());
                dto.setImageUrl(product.getImageUrl());
                dto.setSpecifications(product.getSpecifications());
                
                // Handle null category gracefully
                if (product.getCategory() != null) {
                    dto.setCategoryName(product.getCategory().getName());
                    dto.setCategoryId(product.getCategory().getId());
                }
                
                return dto;
            }).orElse(null);
        } catch (Exception e) {
            System.err.println("Error getting product by ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setName(dto.getName());
        product.setBrand(dto.getBrand());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setDescription(dto.getDescription());
        product.setSpecifications(dto.getSpecifications());
        product.setImageUrl(dto.getImageUrl());
        
        Category category = categoryRepository.findById(dto.getCategoryId())
                               .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);

        productRepository.save(product);
    }

    // Category management methods
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public Category updateCategory(Long id, Category categoryDetails) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            category.setName(categoryDetails.getName());
            category.setDescription(categoryDetails.getDescription());
            category.setImageUrl(categoryDetails.getImageUrl());
            return categoryRepository.save(category);
        }
        return null;
    }

    public boolean deleteCategory(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
