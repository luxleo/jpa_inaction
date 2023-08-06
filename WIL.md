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