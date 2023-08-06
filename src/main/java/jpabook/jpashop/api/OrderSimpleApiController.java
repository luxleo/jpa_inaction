package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderState;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.orders.simplequery.SimpleOrderQueryRepository;
import jpabook.jpashop.response.SimpleOrderQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne(ManyToOne,OneToMany)의 문제를 어떻게 풀 것인가
 * 무한 참조 루프, -> @JsonIgnore
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;
    private final SimpleOrderQueryRepository simpleOrderQueryRepository;

    /**
     * @return
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        return all;
    }

    /**
     * findAll과 같이 복수개의 row가 반환될때
     * stream.map을 이용하여 Dto로 변환하여 반환한다.
     * 1+N+N 쿼리가 나간다.
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        log.info("[ORDERFINDALL] called");
        List<Order> all = orderRepository.findAll(new OrderSearch());
        return all.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }

    /**
     * fetch join 을 이용하여 N+1 -> 1의 단건의 조회로 ㅎㅎ
     * @return
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        log.info("[ORDERS V3]");
        List<Order> all = orderRepository.findAllWithMemberDelivery();
        return all.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }
    @GetMapping("/api/v4/simple-orders")
    public List<SimpleOrderQueryDto> ordersV4() {
        log.info("[ORDERS V4]");
        //return orderRepository.findOrdersDto();
        return simpleOrderQueryRepository.findOrdersDto();
    }


    @Data
    private static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderState orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName(); // LAZY 초기화 (Member table sql init) +1
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getOrderState();
            this.address = order.getDelivery().getAddress(); // LAZY 초기화(Delivery table sql init) +1
        }
    }
}
