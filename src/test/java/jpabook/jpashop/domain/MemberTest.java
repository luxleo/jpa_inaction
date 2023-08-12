package jpabook.jpashop.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberTest {
    @PersistenceContext
    EntityManager em;

    @Test
    void testEntity() {
        Team t1 = new Team("t1");
        Team t2 = new Team("t2");
        em.persist(t1);
        em.persist(t2);

        Member m1 = Member.builder()
                .name("m1")
                .team(t1)
                .build();
        Member m2 = Member.builder()
                .name("m2")
                .team(t1)
                .build();
        Member m3 = Member.builder()
                .name("m3")
                .team(t2)
                .build();
        Member m4 = Member.builder()
                .name("m4")
                .team(t2)
                .build();

        em.persist(m1);
        em.persist(m2);
        em.persist(m3);
        em.persist(m4);

        em.flush();
        em.clear();

        List<Member> selectMFromMemberM = em.createQuery("select m from Member m", Member.class)
                .getResultList();
        for (Member member : selectMFromMemberM) {
            System.out.println("member = " + member);
            System.out.println("member->team = " + member.getTeam());
        }


    }
}