package com.ecommerce.order.product.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.order.product.client.ProductClient;
import com.ecommerce.order.request.response.ProductResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;


@Service
public class ProductService {
	 private static final Logger log =
	            LoggerFactory.getLogger(ProductService.class);

	    private final ProductClient productClient;

	    public ProductService(ProductClient productClient) {
	        this.productClient = productClient;
	    }

	    @CircuitBreaker(
	        name = "productService",
	        fallbackMethod = "fallbackProductService"
	    )
	    public ProductResponse getCategory(Long id) {

	        log.info("Calling Product Service for Product ID: {}", id);

	        return productClient.getProductById(id);
	    }

	    // Fallback method
	    private ProductResponse fallbackProductService(
	            Long id, Throwable e) {

	        log.error(
	            "Circuit breaker fallback triggered for Product ID: {}. Reason: {}",
	            id, e.getMessage()
	        );

	        return null;
	    }
}
