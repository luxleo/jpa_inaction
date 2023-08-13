package jpabook.jpashop;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Team;
import jpabook.jpashop.dto.queryDsl.MemberDto;
import jpabook.jpashop.dto.queryDsl.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static jpabook.jpashop.domain.QMember.member;

@SpringBootTest
@Transactional
public class QueryDslIntermidateTest {
    @Autowired
    EntityManager em;
    JPAQueryFactory query;

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
    void simpleProjection() {
        List<String> fetch = query.select(member.name)
                .from(member)
                .fetch();
    }

    /**
     * tuple도 repository안에서 사용하도록하자
     * service,controller등으로 넘어가면 안된다.
     */
    @Test
    void tupleProjection() {
        List<Tuple> fetch = query.select(member.name, member.age)
                .from(member)
                .fetch();
    }

    /**
     * 생성자 방식으로만 지원한다.
     */
    @Test
    void dtoByJpql() {
        List<MemberDto> resultList = em.createQuery("select new jpabook.jpashop.dto.queryDsl.MemberDto(m.name,m.age) from Member m"
                        , MemberDto.class)
                .getResultList();
    }

    /**
     * 세터,생성,프로퍼티 조회
     */
    @Test
    void dtoByQueryDsl() {
        // 세터로 접근
        List<MemberDto> fetch = query.select(Projections.bean(MemberDto.class,
                        member.name, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : fetch) {
            System.out.println("memberDto = " + memberDto);
        }
        //필드에 직접 접근.
        List<MemberDto> field = query.select(Projections.fields(MemberDto.class, member.name, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : field) {
            System.out.println("memberDto = " + memberDto);
        }
        // jpa와 같이 생성자 이용
        List<MemberDto> constRes = query.select(Projections.constructor(MemberDto.class, member.name, member.age))
                .from(member)
                .fetch();
        for (MemberDto constRe : constRes) {
            System.out.println("constRe = " + constRe);
        }
        // 별칭이 다를떄 .as
        List<UserDto> username = query.select(
                        Projections.fields(UserDto.class,
                                member.name.as("username"), member.age)
                ).from(member)
                .fetch();
        for (UserDto userDto : username) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    void dynamicQuery() {
        String usernameCond = "빈지노";
        Integer ageCond = 0;
        List<Member> fetch = query.selectFrom(member)
                .where(nameEq(usernameCond), ageEq(ageCond))
                .fetch();
        for (Member fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }
    }

    @Test
    void bulk_update() {
        long rowNum = query.update(member)
                .set(member.age, member.age.add(1))
                .execute();
        // bulk연산은 db로 바로 히트 하기 때문에 영속성 컨텍스트 맞춰줘야한다.
        em.flush();
        em.clear();

        List<Member> fetch = query.selectFrom(member)
                .fetch();
        for (Member fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }

        em.flush();
        em.clear();

        long rowNum2 = query.update(member)
                .set(member.name, member.name.concat("_성인"))
                .where(member.age.goe(19))
                .execute();
        System.out.println("rowNum2 = " + rowNum2);

        List<Member> fetch1 = query.selectFrom(member)
                .fetch();
        for (Member member1 : fetch1) {
            System.out.println("member1 = " + member1);
        }
        // delete bulk -> 비성인 삭제
//        query.delete(member)
//                .where(member.name.contains("성인").not())
//                .execute();
        em.flush();
        em.clear();

        List<Member> fetch2 = query.selectFrom(member)
                .fetch();
        for (Member member1 : fetch2) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    void sql_function() {
        List<String> fetch = query.select(Expressions.stringTemplate(
                        "function('replace',{0},{1},{2})"
                        , member.name, "m", "Member"
                )).from(member)
                .fetch();
        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    private BooleanExpression nameEq(String usernameCond) {
        return usernameCond == null ? null : member.name.eq(usernameCond);
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond == null ? null : member.age.eq(ageCond);
    }

    private BooleanExpression composite(String nameCond,Integer ageCond) {
        return nameEq(nameCond).and(ageEq(ageCond));
    }
}
