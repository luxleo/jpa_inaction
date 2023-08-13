package jpabook.jpashop;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.queryDsl.Hello;
import jpabook.jpashop.domain.queryDsl.QHello;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class JpashopApplicationTests {
	@Autowired
	EntityManager em;
	@Test
	void contextLoads() {
		String msg = "Hello queryDSL";
		Hello hello = new Hello(msg);
		em.persist(hello);

		JPAQueryFactory query = new JPAQueryFactory(em);

		QHello qHello = new QHello("h");
		Hello hello1 = query.selectFrom(qHello)
				.fetchOne();

		Assertions.assertThat(hello1).isEqualTo(hello);
	}

}
