package com.turbo.productservice.service;

import com.turbo.productservice.model.Product;
import com.turbo.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private List<Product> productList;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize Mockito mocks

        // Initialize products
        LocalDateTime now = LocalDateTime.now();
        product1 = new Product(1L, "Product 1", "Description 1", new BigDecimal("10.00"), 100, now, now);
        product2 = new Product(2L, "Product 2", "Description 2", new BigDecimal("20.00"), 200, now, now);

        productList = new ArrayList<>();
        productList.add(product1);
        productList.add(product2);
    }

    @Test
    void testCreateProduct() {
        when(productRepository.save(product1)).thenReturn(product1);
        Product createdProduct = productService.createProduct(product1);
        assertEquals(product1, createdProduct);
        verify(productRepository, times(1)).save(product1);
    }

    @Test
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(productList);
        List<Product> allProducts = productService.getAllProducts();
        assertEquals(productList.size(), allProducts.size());
        assertEquals(productList, allProducts);
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductById_ProductExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        Optional<Product> foundProduct = productService.getProductById(1L);
        assertTrue(foundProduct.isPresent());
        assertEquals(product1, foundProduct.get());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductById_ProductNotFound() {
        when(productRepository.findById(3L)).thenReturn(Optional.empty());
        Optional<Product> foundProduct = productService.getProductById(3L);
        assertTrue(foundProduct.isEmpty());
        verify(productRepository, times(1)).findById(3L);
    }

    @Test
    void testUpdateProduct_ProductExists() {
        Product updatedProductDetails = new Product(1L, "Updated Product 1", "Updated Description 1", new BigDecimal("15.00"), 150, LocalDateTime.now(), LocalDateTime.now());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProductDetails); // Return the updated product

        Product updatedProduct = productService.updateProduct(1L, updatedProductDetails);

        assertNotNull(updatedProduct);
        assertEquals("Updated Product 1", updatedProduct.getName());
        assertEquals("Updated Description 1", updatedProduct.getDescription());
        assertEquals(new BigDecimal("15.00"), updatedProduct.getPrice());
        assertEquals(150, updatedProduct.getStockQuantity());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_ProductNotFound() {
        Product updatedProductDetails = new Product(1L, "Updated Product 1", "Updated Description 1", new BigDecimal("15.00"), 150, LocalDateTime.now(), LocalDateTime.now());
        when(productRepository.findById(3L)).thenReturn(Optional.empty());
        Product updatedProduct = productService.updateProduct(3L, updatedProductDetails);
        assertNull(updatedProduct);
        verify(productRepository, times(1)).findById(3L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProduct() {
        productService.deleteProduct(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }
}