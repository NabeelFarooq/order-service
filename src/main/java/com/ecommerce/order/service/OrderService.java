package com.ecommerce.order.service;

import java.util.LinkedHashMap;
import java.util.Map;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.ecommerce.order.product.service.ProductService;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.OutboxEventRepository;
import com.ecommerce.order.request.response.OrderRequest;
import com.ecommerce.order.request.response.OrderResponse;
import com.ecommerce.order.request.response.ProductResponse;

import jakarta.transaction.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class OrderService {
	

	@Autowired
	private Propagator propagator;
	private static final Logger log =
	        LoggerFactory.getLogger(OrderService.class);

	@Autowired
	private Tracer tracer;
	@Autowired
	private ProductService productService;
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

        Map<String, String> carrier = new LinkedHashMap<>();

        Span currentSpan = tracer.currentSpan();

        if (currentSpan != null) {
            propagator.inject(
                currentSpan.context(),
                carrier,
                (map, key, value) -> map.put(key, value)
            );
        }

        outboxEvent.setTraceContext(
            objectMapper.writeValueAsString(carrier)
        );

        outboxEventRepository.save(outboxEvent);
        outboxEventRepository.save(outboxEvent);
        
		return orderResponse;
	}


public String createOrderFiegn(OrderRequest orderRequest) {

    Span span = tracer.currentSpan();

    String traceId = span != null
            ? span.context().traceId()
            : "N/A";

    String spanId = span != null
            ? span.context().spanId()
            : "N/A";

    log.info("[traceId={}, spanId={}] Calling Product Service",
            traceId, spanId);
    // call product service through circuit breaker
    ProductResponse resp =
    		productService.getCategory(
                    (long) orderRequest.getProductId());
    
    if(resp != null) {
    	
    	log.info("[traceId={}, spanId={}] Received product data using Feign Client",
                traceId, spanId);

        log.info("[traceId={}, spanId={}] ========== PRODUCT DETAILS ==========",
                traceId, spanId);

        log.info("[traceId={}, spanId={}] Product ID: {}",
                traceId, spanId, resp.getProductId());

        log.info("[traceId={}, spanId={}] Name: {}",
                traceId, spanId, resp.getName());

        log.info("[traceId={}, spanId={}] SKU: {}",
                traceId, spanId, resp.getSku());

        log.info("[traceId={}, spanId={}] Description: {}",
                traceId, spanId, resp.getDescription());

        log.info("[traceId={}, spanId={}] Price: {}",
                traceId, spanId, resp.getPrice());

        log.info("[traceId={}, spanId={}] Quantity: {}",
                traceId, spanId, resp.getQuantity());

        log.info("[traceId={}, spanId={}] Brand: {}",
                traceId, spanId, resp.getBrand());

        log.info("[traceId={}, spanId={}] Category: {}",
                traceId, spanId, resp.getCategory());

        log.info("[traceId={}, spanId={}] Active: {}",
                traceId, spanId, resp.getActive());

        log.info("[traceId={}, spanId={}] =====================================",
                traceId, spanId);
    } else {
		log.warn("Product details unavailable. Continuing order creation.");
	}
    

    return "Success";
}

    private String generateOrderNumber() {
		String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

		return "ORD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + randomPart;
	}
	 
	
}
