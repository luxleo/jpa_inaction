package jpabook.jpashop.repository.orders.query;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderState;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderFlatDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderState orderStatus;
    private Address address;

    private String itemName;
    private int orderPrice;
    private int orderCount;

    public OrderFlatDto(Long orderId, String name, LocalDateTime orderDate, OrderState orderStatus, Address address, String itemName, int orderPrice, int orderCount) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.orderCount = orderCount;
    }
}
