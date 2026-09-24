package com.ecommerce.order.event;

import org.springframework.stereotype.Component;

import com.ecommerce.order.entity.OrderEntity;
import com.ecommerce.order.util.EventIdGenerator;

@Component
public class OrderEventFactory {

    private final EventIdGenerator eventIdGenerator;

    public OrderEventFactory(EventIdGenerator eventIdGenerator) {
        this.eventIdGenerator = eventIdGenerator;
    }

    public OrderCreatedEvent create(OrderEntity order) {

        OrderCreatedEvent event = new OrderCreatedEvent();

        event.setEventId(eventIdGenerator.generate());
        event.setEventType("ORDER_CREATED");
        event.setOrderId(order.getOrderId());
        event.setCustomerId(order.getCustomerId());
        event.setAmount(order.getAmount());
        event.setDeliveryAddress(order.getDeliveryAddress());
        event.setEventTime(order.getCreatedAt());
        return event;
    }
}