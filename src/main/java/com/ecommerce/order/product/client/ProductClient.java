package com.ecommerce.order.product.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ecommerce.order.request.response.ProductResponse;



@Service
@FeignClient(name = "product-service", configuration = TracingFeignConfig.class)
public interface ProductClient {

	@GetMapping("/product/{id}")
	ProductResponse getProductById(@PathVariable("id") Long id);
}