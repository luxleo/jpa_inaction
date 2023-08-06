package jpabook.jpashop.repository.orders.simplequery;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.response.SimpleOrderQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SimpleOrderQueryRepository {
    private final EntityManager em;

    public List<SimpleOrderQueryDto> findOrdersDto() {
        return em.createQuery(
                "select new jpabook.jpashop.response.SimpleOrderQueryDto(o.id,m.name,o.orderDate,o.orderState,d.address) " +
                        "from Order o " +
                        "join o.member m " +
                        "join o.delivery d", SimpleOrderQueryDto.class
        ).getResultList();
    }

}
