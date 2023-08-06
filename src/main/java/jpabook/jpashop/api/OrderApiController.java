package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderState;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.orders.query.OrderFlatDto;
import jpabook.jpashop.repository.orders.query.OrderItemQueryDto;
import jpabook.jpashop.repository.orders.query.OrderQueryDto;
import jpabook.jpashop.repository.orders.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        // hibernate5Module이 있어야 가능하다.
        for (Order order : all) {
            //order.getMember().getName();
            order.getDelivery().getAddress();

//            List<OrderItem> orderItems = order.getOrderItems();
//            orderItems.stream()
//                    .forEach(o -> o.getItem().getName());
        }
        return all;
    }

    /**
     * dto를 단순히 풀어서 반환할때 필드에 collection이 있다면
     * 이 녀석의 dto도 만들어서 반환해야한다.
     * 똑같이 N+1문제를 일으킨다 (1+N+N+xN[collection수 만큼])
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        return all.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    /**
     * join fetch로 N+1 문제는 해결한다.-> 하지만 join 하는 collection의 수만큼
     * order의 row수가 뻥튀기 되는 문제가 발생한다. -> 해결책은 jpql에 distnct추가
     * 치명적인 문제: paging할수 엄써 ㅠㅠ
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> all = orderRepository.findAllWithItem();
        return all.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
    }

    /**
     * 전략: xToOne의 관계에 있는 녀석들은 join fetch로 가져온다.
     * collection은 LAZY를 그대로 이용해준다.
     * 전역으로 hibernate의 default_batch_size를 설정하여 1:N:M -> 1:1:1이 된다(order:orderItems:Item)
     *
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<Order> all = orderRepository.findAllWithMemberDelivery(offset,limit);
        return all.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
    }

    /**
     * repository에서 바로 loop를 통하여 dto로 반환한다.
     * 1+N issue
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * 쿼리를 1+1으로 만듦
     * orderIds에 해당하는 orderItem을 IN()절로 map으롤 만들어 조회
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findOrderQueryDtos_Opimization();
    }

    /**
     * 장점: 단 한번의 쿼리로 가능하다.
     * 단점: 페이징 처리가 불가능하고, 개발자가 직접 콜렉션을 발라내는 작업을 해주어야한다.
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findOrderQueryDtos_flat();
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(),o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getOrderCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }
    @Getter
    private class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderState orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;
        public OrderDto(Order order){
            this.orderId = order.getId();
            this.name = order.getMember().getName(); // LAZY 초기화 (Member table sql init) +1
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getOrderState();
            this.address = order.getDelivery().getAddress(); // LAZY 초기화(Delivery table sql init) +1
            // Hibernate5Module이 있을때 만 가능하다.
            //order.getOrderItems().stream().forEach(o -> o.getItem().getName());
            //this.orderItems = order.getOrderItems();
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(toList());
        }
    }
    @Getter
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int orderCount;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            orderCount = orderItem.getCount();
        }
    }
}
