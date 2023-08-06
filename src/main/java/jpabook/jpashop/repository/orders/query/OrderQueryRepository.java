package jpabook.jpashop.repository.orders.query;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> orders = findOrders();
        orders.stream()
                .forEach(o->{
                    List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
                    o.setOrderItems(orderItems);
                });
        return orders;
    }
    public List<OrderQueryDto> findOrderQueryDtos_Opimization() {
        List<OrderQueryDto> orders = findOrders();
        List<Long> orderIds = toOrderIds(orders);
        Map<Long, List<OrderItemQueryDto>> collect = toOrderIdItemMap(orderIds);
        orders.stream().forEach(o -> o.setOrderItems(collect.get(o.getOrderId())));

        return orders;
    }

    private Map<Long, List<OrderItemQueryDto>> toOrderIdItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.orders.query.OrderItemQueryDto(oi.order.id,i.name,oi.orderPrice,oi.count) " +
                                "from OrderItem oi " +
                                "join oi.item i " +
                                "where oi.order.id in :orderIds", OrderItemQueryDto.class
                ).setParameter("orderIds", orderIds)
                .getResultList();
        // orderItems를 orderId 별로 그룹화한 리스트 맵 으로 만듬<OrderId, List<Item>>
        Map<Long, List<OrderItemQueryDto>> collect = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        return collect;
    }

    private static List<Long> toOrderIds(List<OrderQueryDto> orders) {
        List<Long> orderIds = orders.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.orders.query.OrderItemQueryDto(oi.order.id,i.name,oi.orderPrice,oi.count) " +
                                "from OrderItem oi " +
                                "join oi.item i " +
                                "where oi.order.id =:orderId", OrderItemQueryDto.class
                ).setParameter("orderId", orderId)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.orders.query.OrderQueryDto(o.id,m.name,o.orderDate,o.orderState,d.address) " +
                        "from Order o " +
                        "join o.member m " +
                        "join o.delivery d", OrderQueryDto.class
        ).getResultList();
    }


    public List<OrderFlatDto> findOrderQueryDtos_flat() {
        return em.createQuery(
                "select new "+
                        "jpabook.jpashop.repository.orders.query.OrderFlatDto(o.id,m.name,o.orderDate,o.orderState,d.address,i.name,oi.orderPrice,oi.count) "+
                "from Order o "+
                        "join o.member m "+
                        "join o.delivery d "+
                        "join o.orderItems oi "+
                        "join oi.item i", OrderFlatDto.class
        ).getResultList();
    }
}
