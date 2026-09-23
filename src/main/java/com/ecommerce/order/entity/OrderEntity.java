package com.ecommerce.order.entity;

import jakarta.persistence.Entity;

@Entity
public class OrderEntity {
	Request:
	{
	    "customerId": 101,
	    "customerName": "Rahul",
	    "productId": 501,
	    "productName": "Laptop",
	    "quantity": 1,
	    "amount": 75000,
	    "deliveryAddress": "Bangalore"
	}

	Response:
	{
	    "orderId": 1001,
	    "status": "CREATED"
	}

	Topic: order-created

	Event:
	{
	    "eventId": "EVT-10001",
	    "eventType": "ORDER_CREATED",
	    "orderId": 1001,
	    "customerId": 101,
	    "amount": 75000,
	    "deliveryAddress": "Bangalore"
	}
}
