package com.ecommerce.order.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.order.entity.OrderEntity;
import com.ecommerce.order.entity.OutboxEvent;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.event.OrderCreatedEvent;
import com.ecommerce.order.event.OrderEventFactory;
import com.ecommerce.order.exception.UnableToPlaceOrderException;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.OutboxEventRepository;
import com.ecommerce.order.request.response.OrderRequest;
import com.ecommerce.order.request.response.OrderResponse;

import jakarta.transaction.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class OrderService {
	private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final OrderEventFactory orderEventFactory;
    private final ObjectMapper objectMapper;
    public OrderService(
            OrderRepository orderRepository,
            OutboxEventRepository outboxEventRepository,
            OrderEventFactory orderEventFactory,
            ObjectMapper objectMapper) {

        this.orderRepository = orderRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.orderEventFactory = orderEventFactory;
        this.objectMapper = objectMapper;
    }
    @Transactional
	public OrderResponse createOrder(OrderRequest orderRequest) {
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
		orderEntity.setOrderStatus(OrderStatus.CREATED);
		
		// unable to create order exception
		OrderEntity orderEntityResp = orderRepository.save(orderEntity);
		if(orderEntityResp.getId() <= 0) {
			throw new UnableToPlaceOrderException("Unable to place order. Please retry later");
		}
		
		OrderResponse orderResponse = new OrderResponse();

		orderResponse.setOrderId(orderEntityResp.getOrderId());
		orderResponse.setStatus(orderEntityResp.getOrderStatus());

		
		System.out.println("OrderService.createOrder().... order created. sending message to kafka");
		
		
		OrderCreatedEvent event =
                orderEventFactory.create(orderEntityResp);

        OutboxEvent outboxEvent = new OutboxEvent();

        outboxEvent.setEventId(event.getEventId());
        outboxEvent.setEventType(event.getEventType());
        outboxEvent.setMessageKey(event.getOrderId());
        outboxEvent.setTopic("order-created");
        outboxEvent.setPayload(
                objectMapper.writeValueAsString(event));
        outboxEvent.setStatus("NEW");
        outboxEvent.setCreatedAt(LocalDateTime.now());

        outboxEventRepository.save(outboxEvent);
        
		return orderResponse;
	}

	private String generateOrderNumber() {
		String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

		return "ORD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + randomPart;
	}
	 
	
}
