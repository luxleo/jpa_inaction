package jpabook.jpashop.service.concurrency_prac;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.concurreny_prac.StockRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
@Transactional
class StockServiceTest {
    @Autowired
    EntityManager em;
    @Autowired
    StockRepository stockRepository;
    @Autowired
    StockService stockService;
    private Long bookId;

    @PostConstruct
    void postconstuct() {
        Book book = new Book();
        book.setName("test Book");
        book.setPrice(10000);
        book.setStockQuantity(300);
        Book save = stockRepository.saveAndFlush(book);
        this.bookId = save.getId();
    }

    @BeforeEach
    void before() {
        Item iniBook = stockRepository.findById(bookId).orElseThrow();
        iniBook.setStockQuantity(300);
        stockRepository.saveAndFlush(iniBook);
        em.clear();
    }



    @Test
    @DisplayName("재고감소 한개의 스레드에서는 정상작동한다.")
    void one_thread_diff() {
        Item findItem = stockRepository.findById(bookId).orElseThrow();
        stockService.decrease(findItem.getId(), 10);

        Assertions.assertThat(findItem.getStockQuantity())
                .isEqualTo(290);
    }

    @Test
    @DisplayName("여러개의 스레드에서 재고감소는 race condition상태에 놓여 비정상 작동한다.")
    void multi_thread_diff() throws InterruptedException {
        int thread_cnt = 10;
        ExecutorService threadPool = Executors.newFixedThreadPool(thread_cnt);
        CountDownLatch latch = new CountDownLatch(thread_cnt);

        for (int i = 0; i < thread_cnt; i++) {
            threadPool.submit(() -> {
                try {
                    stockService.decrease(bookId,1);
//                    item.removeStock(1);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Item item = stockRepository.findById(bookId).orElseThrow();
        System.out.println("item.getStockQuantity() = " + item.getStockQuantity());
        Assertions.assertThat(item.getStockQuantity()).isNotEqualTo(300);
    }

    @Test
    @DisplayName("pessimistic lock")
    void pessi_lock() throws InterruptedException {

        int thread_cnt = 10;
        ExecutorService threadPool = Executors.newFixedThreadPool(thread_cnt);
        CountDownLatch latch = new CountDownLatch(thread_cnt);

        for (int i = 0; i < thread_cnt; i++) {
            threadPool.submit(() -> {
                try {
                    stockService.decrease_pessi_lock(bookId, 1);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Item findBook = stockRepository.findById(bookId).orElseThrow();
        System.out.println("============================");
        System.out.println("findBook.getStockQuantity() = " + findBook.getStockQuantity());
        Assertions.assertThat(findBook.getStockQuantity())
                .isEqualTo(300);
    }
}