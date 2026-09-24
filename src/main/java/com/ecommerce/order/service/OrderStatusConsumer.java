package com.ecommerce.order.service;


import com.ecommerce.order.entity.OrderEntity;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class OrderStatusConsumer {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public OrderStatusConsumer(OrderRepository orderRepository,
                               ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "payment-success",
        groupId = "order-status-service-group"
    )
    @Transactional
    public void onPaymentSuccess(String payload) throws Exception {

        JsonNode event = objectMapper.readTree(payload);

        String orderId = event.get("orderId").asText();

        OrderEntity order = orderRepository.findByOrderNumber(orderId);

        if (order != null) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            System.out.println(
                "Order status updated: " + orderId + " -> CONFIRMED"
            );
        }
    }

    @KafkaListener(
        topics = "payment-failed",
        groupId = "order-status-service-group"
    )
    @Transactional
    public void onPaymentFailed(String payload) throws Exception {

        JsonNode event = objectMapper.readTree(payload);

        String orderId = event.get("orderId").asText();

        OrderEntity order = orderRepository.findByOrderNumber(orderId);

        if (order != null) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            System.out.println(
                "Order status updated: " + orderId + " -> CANCELLED"
            );
        }
    }

    @KafkaListener(
        topics = "order-delivered",
        groupId = "order-status-service-group"
    )
    @Transactional
    public void onOrderDelivered(String payload) throws Exception {

        JsonNode event = objectMapper.readTree(payload);

        String orderId = event.get("orderId").asText();

        OrderEntity order = orderRepository.findByOrderNumber(orderId);

        if (order != null) {
            order.setOrderStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);

            System.out.println(
                "Order status updated: " + orderId + " -> DELIVERED"
            );
        }
    }
}