package jpabook.jpashop.repository;

import jpabook.jpashop.domain.OrderState;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderSearch {
    private String memberName;
    private OrderState orderState;
}
