package jpabook.jpashop.repository.queryDsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.dto.queryDsl.MemberDto;
import jpabook.jpashop.repository.queryDsl.cond.QMemberSearchCond;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QTeam.team;
import static org.springframework.util.StringUtils.hasText;
import static org.thymeleaf.util.StringUtils.isEmpty;

@RequiredArgsConstructor
public class QMemberRepositoryImpl implements QMemberRepositoryCustom {
    private final JPAQueryFactory query;
    @Override
    public List<MemberDto> searchWithCond(QMemberSearchCond searchCond) {
        return query.select(Projections.bean(MemberDto.class,
                        member.name, member.age))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        memberNameEq(searchCond.getMemberName())
                        , teamNameEq(searchCond.getTeamName())
                        , userAgeGoe(searchCond.getAgeGoe())
                        , userAgeLoe(searchCond.getAgeLoe()))
                .fetch();
    }

    /**
     * 조회문에는 조인이 필요하지만, left join이기 때문에
     * 카운트 쿼리에는 조인이 필요없음 -> complex에서 구현한다.
     */
    @Override
    public Page<MemberDto> searchWithCondAndPageSimple(QMemberSearchCond searchCond, Pageable pageable) {
        QueryResults<MemberDto> fetchResults = query.select(Projections.bean(MemberDto.class,
                        member.name, member.age))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        memberNameEq(searchCond.getMemberName())
                        , teamNameEq(searchCond.getTeamName())
                        , userAgeGoe(searchCond.getAgeGoe())
                        , userAgeLoe(searchCond.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults(); // -> fetchResults()하면 페이징에 적합한 녀석들이 나온다.
        List<MemberDto> content = fetchResults.getResults();
        long total = fetchResults.getTotal();
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 조회 쿼리는 복잡하고, 카운트 쿼리 단순한 경우 fetchResults 사용 하지 않고
     * 쿼리 분리하여 사용
     */
    @Override
    public Page<MemberDto> searchWithCondAndPageComplex(QMemberSearchCond searchCond, Pageable pageable) {
        List<MemberDto> content = query.select(Projections.bean(MemberDto.class,
                        member.name, member.age))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        memberNameEq(searchCond.getMemberName())
                        , teamNameEq(searchCond.getTeamName())
                        , userAgeGoe(searchCond.getAgeGoe())
                        , userAgeLoe(searchCond.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<MemberDto> preCnt = query.select(Projections.bean(MemberDto.class,
                        member.name, member.age))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        memberNameEq(searchCond.getMemberName())
                        , teamNameEq(searchCond.getTeamName())
                        , userAgeGoe(searchCond.getAgeGoe())
                        , userAgeLoe(searchCond.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        //.fetch();
//        long count = query.selectFrom(member)
//                .fetchCount();
        // 아래는 현재페이지가 페이지 사이즈보다 작으면 카운트 쿼리를 날리지 않는다.
        return PageableExecutionUtils.getPage(content, pageable, preCnt::fetchCount);
        //return new PageImpl<>(content, pageable, count);
    }

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
