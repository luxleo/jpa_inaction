package jpabook.jpashop;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {
    @Autowired
    EntityManager em;
    JPAQueryFactory query; // 이렇게 em을 바로 이용하여도 multi thread에 걱정없이 할 수 있다.

    @BeforeEach
    void before() {
        query = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member m1 = Member.builder()
                .name("m1")
                .age(10)
                .team(teamA)
                .build();
        Member m2 = Member.builder()
                .name("m2")
                .age(20)
                .team(teamA)
                .build();

        Member m3 = Member.builder()
                .name("m3")
                .age(30)
                .team(teamB)
                .build();
        Member m4 = Member.builder()
                .name("m4")
                .age(40)
                .team(teamB)
                .build();
        em.persist(m1);
        em.persist(m2);
        em.persist(m3);
        em.persist(m4);
    }
    @Test
    public void startJPQL() {
        Member findMember = em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name","m1")
                .getSingleResult();
        assertThat(findMember.getName()).isEqualTo("m1");
    }

    @Test
    public void startQueryDsl() {
        QMember m = new QMember("h"); // 굳이 variable을 지정하지 않아도 된다.

        //컴파일 시점에 오류를 잡을 수 있다. -> jpql의 경우 어렵다.
        Member findMember = query.selectFrom(m)
                .where(m.name.eq("m1")) // 이 처럼 바로 바로 비교하기 때문에 jpql에 비하여 sql인젝션에 안정적이다.
                .fetchOne();
        assertThat(findMember.getName()).isEqualTo("m1");
    }

    @Test
    public void QType() {
//        QMember member = QMember.member;
//        Member findMember = query.selectFrom(member)
//                .where(member.name.eq("m1"))
//                .fetchOne();
        Member m1 = query.selectFrom(member) // simplest form with static import
                .where(member.name.eq("m1"))
                .fetchOne();
        assertThat(m1.getName())
                .isEqualTo("m1");
    }

    @Test
    void fetch_result() {
        Member member1 = query.selectFrom(member)
                .where(member.id.eq(1L))
                .fetchOne();
        System.out.println("member1 = " + member1);
        QueryResults<Member> memberQueryResults = query.selectFrom(member)
                .fetchResults();
        System.out.println("memberQueryResults.getTotal() = " + memberQueryResults.getTotal());
        System.out.println("memberQueryResults.getOffset() = " + memberQueryResults.getOffset());
        System.out.println("memberQueryResults.getLimit() = " + memberQueryResults.getLimit());

        List<Member> results = memberQueryResults.getResults();
        for (Member result : results) {
            System.out.println("member = " + result);
        }

        long count = query.selectFrom(member)
                .fetchCount();
        System.out.println("count = " + count);
    }

    @Test
    void sort_qurey() {
        em.persist(new Member(null,100));
        em.persist(new Member("a1",100));
        em.persist(new Member("a2",100));

        List<Member> result = query.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.name.asc().nullsLast(), member.age.desc())
                .fetch();
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    void paging_test() {
        QueryResults<Member> memberQueryResults = query.selectFrom(member)
                .orderBy(member.createdBy.desc(), member.lastModifiedBy.desc())
                .offset(1)
                .limit(5)
                .fetchResults();
        System.out.println("memberQueryResults.getOffset() = " + memberQueryResults.getOffset());
        System.out.println("memberQueryResults.getLimit() = " + memberQueryResults.getLimit());
        System.out.println("memberQueryResults.getTotal() = " + memberQueryResults.getTotal());
        System.out.println("memberQueryResults.getResults().size() = " + memberQueryResults.getResults().size());
    }

    @Test
    void aggregation() {
        List<Tuple> result = query.select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();
        Tuple tuple = result.get(0);
        System.out.println("tuple.get(member.count()) = " + tuple.get(member.count()));
        System.out.println("tuple.get(member.count()) = " + tuple.get(member.age.sum()));
        System.out.println("tuple.get(member.count()) = " + tuple.get(member.age.avg()));
        System.out.println("tuple.get(member.count()) = " + tuple.get(member.age.max()));
    }

    @Test
    void group() {
        List<Tuple> result = query.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team) // team의 필드 없이 넘기면 team.id()로 조회
                .fetch();
        Tuple team1 = result.get(0);
        Tuple team2 = result.get(1);

        System.out.println("team1.get(team.name) = " + team1.get(team.name) + team1.get(member.age.avg()));
        System.out.println("team2.get(team.name) = " + team2.get(team.name));
    }

    /**
     * teamA의 모든 회원
     */
    @Test
    void normal_join() {
        List<Member> teamA = query.selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .orderBy(member.name.desc())
                .fetch();
        for (Member member1 : teamA) {
            System.out.println("member1.getName() = " + member1.getName());
        }
    }

    /**
     * 팀이름과 같은 이름의 회원 조회
     */
    @Test
    void cross_join() {
        em.persist(new Member("teamA", 0));
        em.persist(new Member("teamB", 0));
        em.persist(new Member("teamC", 0));

        List<Member> result = query.select(member)
                .from(member, team)
                .where(member.name.eq(team.name))
                .fetch();
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    /**
     * 회원과 팀을 조인 조회 하면서 팀의 이름이 'teamA' 인 경우 조회한다.
     * 이때 회원은 모두 조회
     */
    @Test
    void on_join() {
        List<Tuple> teamA = query.select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();
        for (Tuple tuple : teamA) {
            System.out.println("\n");
            System.out.println("tuple.get(member).getName() = " + tuple.get(member).getName());
            if(tuple.get(team) == null) continue;
            System.out.println("tuple.get(team).getName() = " + tuple.get(team).getName());
        }
    }

    /**
     * 2. 연관관계 없는 엔티티 외부 조인
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
     */
    @Test
    @DisplayName("일반적인 fk,pk 조인이 아닌, on 조건으로 막 조인 구현")
    void on_join_no_condition() {
        em.persist(new Member("teamA", 0));
        em.persist(new Member("teamB", 0));
        em.persist(new Member("teamC", 0));

        List<Tuple> fetch = query.select(member, team)
                .from(member)
                .leftJoin(team).on(member.name.eq(team.name))
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println();
            System.out.println("tuple = " + tuple);

        }
    }
    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    void none_fetch_join() {
        em.flush();
        em.clear();
        Member member1 = query.selectFrom(member)
                .where(member.name.eq("m1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("로딩 됐슈?").isFalse();
    }
    @Test
    void fetch_join() {
        em.flush();
        em.clear();
        Member member1 = query.selectFrom(member)
                .join(member.team,team).fetchJoin()
                .where(member.name.eq("m1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("로딩 됐슈?").isTrue();
    }

    @Test
    void subquery_where() {
        QMember memberSub = new QMember("for_sub_query");

        //나이가 가장 많은 회원
        Member member1 = query.selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetchOne();
        System.out.println("member1 = " + member1);

        //나이가 평균 이상인 회원
        List<Member> res2 = query.select(member)
                .from(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();
        for (Member member2 : res2) {
            System.out.println("member2 = " + member2);
        }
    }

    @Test
    void subquery_select() {
        QMember memberSub = new QMember("for_sub_query");
        List<Tuple> res1 = query
                .select(member, JPAExpressions
                        .select(memberSub.age.avg())
                        .from(memberSub))
                .from(member)
                .fetch();
        for (Tuple tuple : res1) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void case_query() {
        // 심플한 경우 when.then사용
        List<String> res1 = query.select(member.age
                        .when(10).then("10yr")
                        .when(20).then("20yr")
                        .otherwise("elder"))
                .from(member)
                .fetch();
        for (String s : res1) {
            System.out.println("s = " + s);
        }
        //복잡한 경우 CaseBuilder사용
        List<String> res2 = query.select(
                        new CaseBuilder()
                                .when(member.age.between(0, 10)).then("1tier")
                                .when(member.age.between(11, 20)).then("2tier")
                                .otherwise("3tier")
                )
                .from(member)
                .fetch();
        for (String s : res2) {
            System.out.println("s2 = " + s);
        }
    }

    @Test
    void constant_concat() {
        List<Tuple> a = query.select(member.name, Expressions.constant("A"))
                .from(member)
                .fetch();
        for (Tuple tuple : a) {
            System.out.println("tuple = " + tuple);
        }
        // {member name}_{member_age}
        List<String> fetch = query.select(member.name.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();
        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }
}
