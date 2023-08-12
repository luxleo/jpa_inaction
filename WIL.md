## MVC
    1. @RestController에서 @RequestBody는 json등으로 들어온 req을 object로 매핑한다.
    2. DTO에는 lombok의 다양한 어노테이션 편의상 사용가능하다.
        그러나 엔티티에는 보수적으로 잘 설계하자(getter 정도로 충분하다면 최소로)
    3. update시에는 변경감지를 이용하자.
    4. 커맨드와 쿼리를 분리하는 정책을 가지자.
    5. 엔티티를 그대로 반환하는 컨트롤러 x -> 응답, 요청 dto따로 작성할것
        * entity를 그대로 반환할시에 엔티티의 변화가 전체 api스펙에 영향을 미친다.
        * 또한 전채 "count"와 같은 새로운 요구 사항이 있을때 대처가 어렵다.
        * 다양한 api의 요구사항을 만족시키는 것은 불가능에 수렴한다.
## JPA
    0. LAZY 전략:
        Order내에 외부테이블 참조(Member,Delivery,OrderItems)는 
        LAZY전략에 따라 byteBuddy로 구현한 프록시 객체를 사용한다.
        이때 order.getMember().getName()으로 LAZY 전략 초기화시
        jpa가 참조 테이블을 찌르게 되는데 @JsonIgnore로 json형식 변환시 참조를 막아두면
        오류가 발생 ...  -> 해결은 hybernate5Module로 프록시의 경우 무시하게 하는 전략이있다.
    1. stream의 용례: 
        findAll과 같이 복수개의 row가 반환될때
        stream.map을 이용하여 Dto로 변환하여 반환한다.
    2. 실무 문제의 90%는 N+1문제
        전략:
            기본적으로 xToOne의 경우 fetch=LAZY를 깔고 들어간다.(조회 하지 않고 가져옴== 빈 값)
            필요한 경우 fetch join을 이용하여 값을 채워 넣어준다.
        보강: 
            fit하게 바로 dto로 넘겨줄경우 join fetch -> fetch(패치 조인은 엔티티를 select할 때만 가능)
            가능하면 따로 repository를 빼는게 유지 보수 편하다. (repository.orders.simplequery)
    3. collection을 조인 할때
        : 생짜로 해주면 .... 후덜덜한 1+xN(x == 콜렉션의 수)을 마주할것
        1. join fetch로 쿼리를 하나로 날려준다.
        1-1: select distinct o from Order o...를 통하여
            콜렉션 기준으로 뻥튀기 되는 수를 잡아준다.
        치명적인 문제: 페이징 처리가 불가능하다 ,,, -> 엄밀히 말하면 다 퍼와서 메모리 내에서 페이징 처리한다.
        ... 결과는 무시무시 한 메모리 사용으로 -> 앱이 죽는다^^
    4. collection 조인시 페이징 처리 방법
        1. xToOne(ManyToOne,OneToOne)은 join fetch로 다 가져온다. + jpql에 distinct적용
        2. collection 프로퍼티의 경우: application.yml에 hibernate.default_batch_size = 100등
        으로 설정하여 해결
        3. 또는 프로퍼티나, 엔티티 클래스에 @BatchSize()로 디테일하게 적용
        4. 결과: 1+N의 쿼리 갯수가 -> 1+1로 줄어든다. 
        5. 원리: IN()절에 pk가 들어가서 순식간에 다 가져온다.
        6. 결론: ToOne은 join fetch로 쿼리 다운, Collection은 default_batch_size 로 해결
    5. OSIV(open session in view)
        1. spring.jpa.open-in-view: true인 경우 Controller에 있는 경우 응답이 반환 되기까지(혹은 view를 반환하기 까지) 
            db 커넥션을 풀어주지 않는다. lazy loading등을 처리하기 위해 -> connection dry발생
        2. spring.jpa.open-in-view; false인 경우 @Transaction내부에서 작업(트랜잭션)이 끝나면 바로 반환한다.
            lazy fetch등을 해결하기 위해 @Transaction(readOnly=true)를 가지는 별도의 윈도우뷰를 위한 @Service를 따로 만든다.
        3. 용례: 실시간 데이터등 복잡한 조회(주로 장애는 삭제,추가등의 간단한 업데이트가 아닌 조회에서 발생)를 가지는 경우 
            spring.jpa.open-in-view:false로 설정
            ADMIN:등 이용자가 작으면 유지 보수성(lazy loading등 복잡한 쿼리를 처리하기 위한 별도의 코드가 필요없음)을 높이기위해
            true로 설정한다.
# spring data jpa
    ./gradlew dependencies --configuration compileClasspath  -> 의존관계 확인
## @PersistenceContext
    jpa는 영속성 컨텍스트로 관리한다.
    private final EntityManger em;과 같이 영속성을 주입해준다.
    써야하는 이유:
        스프링은 싱글톤으로 관리하는데 이떄 thread-safe를 보장 해야한다.
        @PersistenceContext로 설정시 thread-safe를 보장해준다.(프록시 기술로 매번 생성해주기 때문)
## CRUD
    crud에서 update는 jpa에서는 기본적으로 변경감지 기능을 이용한다.
## @Repository
    이 어노테이션은 @ComponentScan + JPA 공통 예외 처리를 가능하게 해주는 부분까지 한다.
## Spring data Jpa
    인터페이스로 만드는데 어떻게 @Autowired와 같은 주입이 되나 -> spring data jpa가 프록시 기술로 클래스 만들어준다.
## 쿼리 메소드
    1.메소드 이름으로 쿼리 생성 가능
        조회: find…By ,read…By ,query…By get…By,
        https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
        #repositories.query-methods.query-creation
        예:) findHelloBy 처럼 ...에 식별하기 위한 내용(설명)이 들어가도 된다.
        COUNT: count…By 반환타입 long
        EXISTS: exists…By 반환타입 boolean
        삭제: delete…By, remove…By 반환타입 long
        DISTINCT: findDistinct, findMemberDistinctBy
        LIMIT: findFirst3, findFirst, findTop, findTop3
    2. named query -> @Query생략 하고도 가능하다. 이유 => 엔티티에서 먼저 jpa기반 @NamedQuery를 탐색하므로
        named query의 장점은 어플리케이션 실행시점에 오류 판단이 가능하다 => 기존의 jpql은 string이라서
        잡아 낼수 없지만 named query의 경우 parsing을 먼저 진행하기 때문이다.
    3. 값 타입 조회
        값:    
            @Query("select m.name from Member m")
            List<String> findByUserName();

        dto:
            @Query("select new jpabook.jpashop.repository.springdatajpa.dto.MemberDto(m.name,m.age,m.id) from Member m")
            List<MemberDto> findMemberAsDto();
    4. 반환 타입: 오버로딩 하듯이 작성이 가능하다.
        
        컬렉션
            결과 없음: 빈 컬렉션 반환
        단건 조회
            결과 없음: null 반환
        결과가 2건 이상: javax.persistence.NonUniqueResultException 예외 발생
        > 참고: 단건으로 지정한 메서드를 호출하면 스프링 데이터 JPA는 내부에서 JPQL의
            Query.getSingleResult() 메서드를 호출한다. 이 메서드를 호출했을 때 조회 결과가 없으면
            javax.persistence.NoResultException 예외가 발생하는데 개발자 입장에서 다루기가 상당히 불편하
            다. 스프링 데이터 JPA는 단건을 조회할 때 이 예외가 발생하면 예외를 무시하고 대신에 null 을 반환한다.
    5. 페이징 처리(JPA):
        필수 조건: 모든 엔티티카운트, 페이징 조회시 orderby
    6. 페이징 처리(spring data jpa):
        코드:
            @Query(value = "select m from Member m left join m.team",countQuery = "select count(m.id) from Member m")
            Page<Member> findByUserName(String name, Pageable pageable);
        두 번째 파라미터로 받은 Pageable 은 인터페이스다. 따라서 실제 사용할 때는 해당 인터페이스를 구현한
        org.springframework.data.domain.PageRequest 객체를 사용한다.
        카운트 쿼리 분리(이건 복잡한 sql에서 사용, 데이터는 left join, 카운트는 left join 안해도 됨)
        실무에서 매우 중요!!!
        > 참고: 전체 count 쿼리는 매우 무겁다.

        반환타입:
            Page -> 얘만 count 쿼리 나간다.
            Slice (count X) 추가로 limit + 1을 조회한다. 그래서 다음 페이지 여부 확인(최근 모바일 리스트 생각해보
                면 됨)
            List (count X)

    7. 벌크 업데이트:
        bulk 연산시 바로 db로 가기때문에 , em.flush()&&em.clear()로 하거나 @Modifing(clear=true)")
            @Modifying(clearAutomatically = true) //이거 없으면 무조건 조회로 생각한다.
            @Query("update Member m set m.age = m.age+1 where m.age>:age")
            int sample_updateMemberAge(int age);
    8. entity graph: fetch join을 jpql없이 간단하게 할 수있도록한다. but웬만 하면 jpql짜고 간단한거에만 적용하자.
            @Override -> JpaRepository override
            @EntityGraph(attributePaths = {"team"})
            List<Member> findAll();
        
            @Query("select m from Member m join fetch m.team") -> 직접
            List<Member> manualJoinFetch();
        
            @EntityGraph(attributePaths = {"team"}) -> @EntityGraph이용 
            @Query("select m from Member m ")
            List<Member> annotJoinFetch();
        
            @EntityGraph(attributePaths = {"team"}) -> 쿼리메소드 + @EntityGraph이용
            List<Member> findEntityGraphByName(@Param("name") String name); 
    9. @QueryHints, @Lock
        1.@QueryHints: jpa에게 힌트를 제공한다.
            @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value ="true"))
            Member findReadOnlyByUsername(String username); -> jpa의 dirty checking 적용 안함
            
            page count query예시
            @QueryHints(value = { @QueryHint(name = "org.hibernate.readOnly",
            value = "true")},
            forCounting = true)
            Page<Member> findByUsername(String name, Pageable pageable);
        2.@Lock : 데이터 락을 걸때 사용하자 별일 아니면 사용하지 않는다(락을 걸면 성능 저하 이슈) -> 자세한 내용은 jpa책 참조
            @Lock(LockModeType.PESSIMISTIC_WRITE)
            List<Member> findByUsername(String name);
    10. 사용자 정의 repository: 아래와 같은 문제를 해결하기 위해 사용한다.
        repository에 JPA 직접 사용( EntityManager )
        스프링 JDBC Template 사용
        MyBatis 사용
        데이터베이스 커넥션 직접 사용 등등...
        Querydsl 사용
        사용법:
        1. custom 기능을 넣을 interface 구현
        2. MemberRepositoryImpl으로 커스텀 interface 구현=> 이건 관례니까 명명 규칙 지키자.
        3. spring data jpa repository에 extends 해준다.
        