package com.ecommerce.order.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.enums.OrderStatus;
import com.ecommerce.exception.UnableToPlaceOrderException;
import com.ecommerce.kafka.KafkaEvent;
import com.ecommerce.kafka.PublishToKafka;
import com.ecommerce.order.entity.OrderEntity;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.request.response.OrderRequest;
import com.ecommerce.order.request.response.OrderResponse;

import tools.jackson.databind.ObjectMapper;

@Service
public class OrderService {
	@Autowired
	OrderRepository orderRepository;
	@Autowired
	PublishToKafka publishToKafka;
	public OrderResponse createOrderForCustomer(OrderRequest orderRequest) {
		OrderEntity orderEntity = new OrderEntity();
		orderEntity.setOrderNumber(generateOrderNumber());
		orderEntity.setCustomerId(orderRequest.getCustomerId());
		orderEntity.setProductId(orderRequest.getProductId());
		orderEntity.setCustomerName(orderRequest.getCustomerName());
		orderEntity.setProductName(orderRequest.getProductName());
		orderEntity.setQuantity(orderRequest.getQuantity());
		orderEntity.setOrderDate(LocalDateTime.now());
		orderEntity.setAmount(orderRequest.getAmount());
		orderEntity.setDeliveryAddress(orderRequest.getDeliveryAddress());
		orderEntity.setOrderStatus(OrderStatus.CONFIRMED);
		
		// unable to create order exception
		OrderEntity orderEntityResp = orderRepository.save(orderEntity);
		if(orderEntityResp.getId() <= 0) {
			throw new UnableToPlaceOrderException("Unable to place order. Please retry later");
		}
		
		OrderResponse orderResponse = new OrderResponse();

		orderResponse.setOrderId(orderEntityResp.getOrderId());
		orderResponse.setStatus(orderEntityResp.getOrderStatus());

		
		System.out.println("OrderService.createOrderForCustomer().... order created. sending message to kafka");
		String data = objToJson(orderResponse);
		
		//kafkaService.sendMessage("order-created", data);
		return orderResponse;
	}

	private String generateOrderNumber() {
		String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

		return "ORD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + randomPart;
	}
	// if its a common method, you can move it to outside
	private String objToJson(OrderResponse orderResponse) {
		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(orderResponse);
		return json;
	} 
	
	
}
