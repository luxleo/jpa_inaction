# spring data jpa 2
## domain class convertor

# queryDSL
    ./gradlew clean -> build 파일 전부 지워준다.
    ./gradlew compileJava -> queryDSL 포함하여 build해준다.
    .gitignore에 build파일은 제거해주자 (QType 같은거)
# queryDSL
    JPAQueryFactory query; // 이렇게 em을 바로 이용하여도 multi thread에 걱정없이 할 수 있다.
    QMember m = new QMember("h"); // 굳이 variable을 지정하지 않아도 된다.

        //컴파일 시점에 오류를 잡을 수 있다. -> jpql의 경우 어렵다. 
        Member findMember = query.selectFrom(m)
                .where(m.name.eq("m1")) // 이 처럼 바로 바로 비교하기 때문에 jpql에 비하여 sql인젝션에 안정적이다.
                .fetchOne();
    query작성 방법:
        member.username.eq("member1") // username = 'member1'
        member.username.ne("member1") //username != 'member1'
        member.username.eq("member1").not() // username != 'member1'
        member.username.isNotNull() //이름이 is not null
        member.age.in(10, 20) // age in (10,20)
        member.age.notIn(10, 20) // age not in (10, 20)
        member.age.between(10,30) //between 10, 30
        member.age.goe(30) // age >= 30
        member.age.gt(30) // age > 30
        member.age.loe(30) // age <= 30
        member.age.lt(30) // age < 30
        member.username.like("member%") //like 검색
        member.username.contains("member") // like ‘%member%’ 검색
        member.username.startsWith("member") //like ‘member%’ 검색
    AND 조건을 파라미터로 처리: and 의 경우 ,로 대신할 수 있다.
    +이 경우 null 값은 무시 메서드 추출을 활용해서 동적 쿼리를 깔끔하게 만들 수 있음 뒤에서 설명
        selectFrom(member)
            .where(member.username.eq("member1"),
                member.age.eq(10))
            .fetch();

    쿼리 결과 조회:

    fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
    fetchOne() : 단 건 조회 -> NonUniqueResultException한건 아니면 예외 발생
    결과가 없으면 : null
    결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException
    fetchFirst() : limit(1).fetchOne()
    fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행
    fetchCount() : count 쿼리로 변경해서 count 수 조회
    
    쿼리 그룹바이:
        query.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team) // team의 필드 없이 넘기면 team.id()로 조회
                .fetch();
    on-join:
        연관관계와 무관한 매핑도 가능하다.
    서브쿼리: select절, where절에서 사용가능, 이때 다른 alias QType객체를 두어야한다. + JpaExpressions사용한다. -> static import 가
        한게:
            from 절의 서브쿼리 한계
            JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다. 당연히 Querydsl
            도 지원하지 않는다. 하이버네이트 구현체를 사용하면 select 절의 서브쿼리는 지원한다. Querydsl도 하
            이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원한다.
        from 절의 서브쿼리 해결방안
            1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
            2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
            3. nativeSQL을 사용한다.
## 프록시 결과 반환 dto
            // 세터로 접근
        List<MemberDto> fetch = query.select(Projections.bean(MemberDto.class,
                        member.name, member.age))
                .from(member)
                .fetch();
        //필드에 직접 접근.
        List<MemberDto> field = query.select(Projections.fields(MemberDto.class, member.name, member.age))
                .from(member)
                .fetch();
        // jpa와 같이 생성자 이용
        List<MemberDto> constRes = query.select(Projections.constructor(MemberDto.class, member.name, member.age))
                .from(member)
                .fetch();
        // 별칭이 다를떄 .as
        List<UserDto> username = query.select(
                        Projections.fields(UserDto.class,
                                member.name.as("username"), member.age)
                ).from(member)
                .fetch();
        //@QueryProjection -> 으로 생성자 방식으로 접근 할때 컴파일 오류로 잡아낼 수 있으나
        여러 레이어에서 사용되는 dto가 queryDsl에 의존적이게 된다. 트레이드 오프 고려하여 사용
## dynamic query + with BooleanExpression

## bulk update and Persistence Context

## sql function