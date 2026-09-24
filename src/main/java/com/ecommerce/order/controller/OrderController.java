package com.ecommerce.order.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.order.request.response.OrderRequest;
import com.ecommerce.order.request.response.OrderResponse;
import com.ecommerce.order.service.OrderService;

@RestController
public class OrderController {
	@Autowired
	OrderService orderService;
	@PostMapping("orders")
	public ResponseEntity<OrderResponse> createOrderForCustomer(@RequestBody OrderRequest orderRequest) {
		OrderResponse orderResponse = orderService.createOrder(orderRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
	}
}
