package jpabook.jpashop.service.concurrency_prac;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.concurreny_prac.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;
    private final JPAQueryFactory query;

    @Transactional
    public void decrease(Long id, Integer diff) {
        Item item = stockRepository.findById(id).orElseThrow(IllegalAccessError::new);
        item.removeStock(diff);
        stockRepository.saveAndFlush(item);
    }

    @Transactional
    public void decrease_pessi_lock(Long id, Integer diff) {
        Item findItem = stockRepository.findByIdWithPessimisticLock(id);
        findItem.removeStock(diff);
        stockRepository.saveAndFlush(findItem);
    }
}
