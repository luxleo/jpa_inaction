package jpabook.jpashop;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InitDb {
    private final InitService initService;

    @PostConstruct
    void init() {
        initService.dbInit();
        initService.dbInit2();
    }
    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;

        public void dbInit() {
            Member member = createMember("빈지노","서울","가로수길1","1111");
            em.persist(member);

            Book book = createBook("JPA1 book", 10000, 100);

            Book book2 = createBook("JPA2 book", 20000, 100);

            em.persist(book);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book, 10000, 10);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 10);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        private static Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }

        private static Book createBook(String bookName, int price, int stockQuantity) {
            Book book = new Book();
            book.setName(bookName);
            book.setPrice(price);
            book.setStockQuantity(stockQuantity);
            return book;
        }

        private static Member createMember(String username,String city, String street,String zipcode) {
            Member member = new Member();
            member.setName(username);
            member.setAddress(new Address(city,street, zipcode));
            return member;
        }

        public void dbInit2() {
            Member member = createMember("이센스","서울","가로수길2","1112");
            em.persist(member);

            Book book = createBook("Spring1 book", 20000, 100);

            Book book2 = createBook("Spring2 book", 30000, 100);

            em.persist(book);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book, 15000, 10);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 25000, 10);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

    }
}
