package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderState;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.fail;


@SpringBootTest
@Transactional
class OrderServiceTest {
    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Test
    void 상품주문() throws Exception{
        // given
        Member member = getMember();

        Book book = getBook("dragon", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order createdOrder = orderRepository.findOne(orderId);
        Assertions.assertEquals( OrderState.ORDER, createdOrder.getOrderState());
        Assertions.assertEquals( 1, createdOrder.getOrderItems().size());
        Assertions.assertEquals( 10000*orderCount, createdOrder.getTotalPrice());
        Assertions.assertEquals(8, book.getStockQuantity());

    }
    @Test
    void 상품주문_재고초과() throws Exception {
        // given
        Member member = getMember();
        Book book = getBook("dragon", 10000, 10);

        int orderCount = 11;
        //when

        Assertions.assertThrows(NotEnoughStockException.class, () -> orderService.order(member.getId(), book.getId(), orderCount));
    }
    @Test
    void 주문취소() throws Exception {
        // given
        Member member = getMember();
        Book book = getBook("dragon", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        //when
        orderService.cancleOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        Assertions.assertEquals(OrderState.CANCLE, getOrder.getOrderState());
        Assertions.assertEquals(10, book.getStockQuantity());
    }
    private Book getBook(String name, int price, int quantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(quantity);
        em.persist(book);
        return book;
    }

    private Member getMember() {
        Member member = new Member();
        member.setName("kim");
        member.setAddress(new Address("busan","new_hwamuyeong","1234-1234"));
        em.persist(member);
        return member;
    }



}