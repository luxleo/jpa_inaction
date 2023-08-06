package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderState;
import jpabook.jpashop.response.SimpleOrderQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static jpabook.jpashop.domain.QOrder.order;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long orderId) {
        return em.find(Order.class, orderId);
    }
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery("select o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d",Order.class)
                .getResultList();
    }

    public List<Order> findAll(OrderSearch orderSearch) {
//        return em.createQuery("select o from Order o join o.member m" +
//                        " where o.orderState = :status" +
//                        " and m.name like :name", Order.class)
//                .setParameter("status", orderSearch.getOrderState())
//                .setParameter("name", orderSearch.getMemberName())
//                .getResultList();
        return query.selectFrom(order)
                .where(likeMemberName(orderSearch.getMemberName()), likeOrderState(orderSearch.getOrderState()))
                .fetch();
    }

    /**
     * fetch join은 엔티티끼리 조회 할때 가능하다.
     * select 절에 엔티티가 아니 므로 join fetch -> join으로 수정
     * 그런데 이렇게 fit한 녀석은 일반적으로 적용이 힘들다 -> 따로 레포지토리를 빼서 전용으로 만들어 주자
     */
    public List<SimpleOrderQueryDto> findOrdersDto() {
        return em.createQuery(
                "select new jpabook.jpashop.response.SimpleOrderQueryDto(o.id,m.name,o.orderDate,o.orderState,d.address) " +
                        "from Order o "+
                        "join o.member m " +
                        "join o.delivery d", SimpleOrderQueryDto.class
        ).getResultList();
    }

    public List<Order> findAllWithItem() {
//        return em.createQuery(
//                "select o from Order o " +
//                        "join fetch o.member m " +
//                        "join fetch o.delivery d " +
//                        "join fetch o.orderItems oi " +
//                        "join fetch oi.item i", Order.class
//        ).getResultList();
        return em.createQuery(
                "select distinct o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d " +
                        "join fetch o.orderItems oi " +
                        "join fetch oi.item i ", Order.class
        ).getResultList();
    }

    private BooleanExpression likeOrderState(OrderState orderState) {
        if (orderState != null) {
            return order.orderState.stringValue().like(orderState.name());
        }
        return null;
    }

    private BooleanExpression likeMemberName(String memberName) {
        if (StringUtils.hasText(memberName)) {
            return order.member.name.like("%"+memberName+"%");
        }
        return null;
    }


    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                        "select o from Order o " +
                                "join fetch o.member m " +
                                "join fetch o.delivery d", Order.class
                ).setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
