package com.ecommerce.order.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ecommerce.order.request.response.OrderRequest;
import com.ecommerce.order.request.response.OrderResponse;
import com.ecommerce.order.service.OrderService;

@RestController
@EnableFeignClients
public class OrderController {
	private static final Logger log =
	        LoggerFactory.getLogger(OrderController.class);

	
	@Autowired
	OrderService orderService;
	@PostMapping("orders")
	public ResponseEntity<OrderResponse> createOrderForCustomer(@RequestBody OrderRequest orderRequest) {
		OrderResponse orderResponse = orderService.createOrder(orderRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
	}
	@PostMapping("orders/create")
	public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {
		
		String resp = orderService.createOrderFiegn(orderRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(resp);
	}
}
