package jpabook.jpashop.repository.queryDsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.queryDsl.cond.QMemberSearchCond;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QTeam.team;
import static org.springframework.util.StringUtils.hasText;
import static org.thymeleaf.util.StringUtils.isEmpty;

@Repository
@RequiredArgsConstructor
public class QMemberJpaRepository {
    private final EntityManager em;
    private final JPAQueryFactory query; // 이런 식으로 빈으로 등록해두면 바로 주입 받은 싱글톤 객체를 사용

    public List<Member> findAll(QMemberSearchCond searchCond) {
        return query.selectFrom(member)
                .where(
                        memberNameEq(searchCond.getMemberName())
                        , teamNameEq(searchCond.getTeamName())
                        , userAgeGoe(searchCond.getAgeGoe())
                        , userAgeLoe(searchCond.getAgeLoe())
                )
                .leftJoin(member.team, team)
                .fetch();
    }

    /**
     * return type이 BooleanExpression이면 조립이 가능하다.
     */
    private BooleanExpression ageBetween(Integer ageLoe, Integer ageGoe) {
        return userAgeLoe(ageLoe).and(userAgeGoe(ageGoe));
    }
    private BooleanExpression userAgeLoe(Integer ageLoe) {
        return ageLoe == null ? null : member.age.loe(ageLoe);
    }

    private BooleanExpression userAgeGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }

    private BooleanExpression teamNameEq(String teamName) {
        return isEmpty(teamName)? null: member.name.eq(teamName);
    }

    private BooleanExpression memberNameEq(String memberName) {
        return hasText(memberName)? member.name.eq(memberName) : null;
    }

}
