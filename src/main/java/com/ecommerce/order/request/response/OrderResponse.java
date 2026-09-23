package com.ecommerce.order.request.response;

import com.ecommerce.enums.OrderStatus;

public class OrderResponse {
	String orderId;
	OrderStatus status;
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public OrderStatus getStatus() {
		return status;
	}
	public void setStatus(OrderStatus status) {
		this.status = status;
	}
	
}
