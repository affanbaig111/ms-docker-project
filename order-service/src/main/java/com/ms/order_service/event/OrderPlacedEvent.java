package com.ms.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class OrderPlacedEvent {
    private String orderId;

    public OrderPlacedEvent(String orderId) {
        this.orderId = orderId;
    }

    // Getter and Setter
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}