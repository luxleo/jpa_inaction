package jpabook.jpashop.repository.concurreny_prac;

import jakarta.persistence.LockModeType;
import jpabook.jpashop.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<Item,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i where i.id = :id")
    Item findByIdWithPessimisticLock(@Param("id") Long id);
}
