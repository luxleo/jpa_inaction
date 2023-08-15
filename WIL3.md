# queryDSL + spring data jpa
## 페이징
    fetchResults() -> 카운트쿼리 + 조회쿼리 : 단순한 경우(조인이 가벼운 경우)
    fetch() + fetchCount() => 각각 나눈다. : 조회 쿼리 많은 조인이 있고 left join일때
    최적화 by 스프링 데이터:
        return PageableExecutionUtils.getPage(content, pageable, preCnt::fetchCount);
        위의 경우 현재 페이지의 사이즈가 페이지 사이즈 보다 작은경우 카운트 쿼리를 날리지 않는다.
    컨트롤러 구현:
    @GetMapping("/v3/members")
    public Page<MemberDto> searchMemberV3(QMemberSearchCond searchCond, Pageable pageable) {
        return memberRepository.searchWithCondAndPageComplex(searchCond, pageable);
    }
    요청 url:http://localhost:8080/q/v2/members?page=1&size=10 => 개쩐다.

    요청 url:http://localhost:8080/q/v3/members?page=1&size=104 => 개쩐다.
    =>이 경우 PageableExecutionUtils를 이용하여 요청 페이지 사이즈보다 현재 페이지 사이즈가 작아
    '카운트 쿼리가 나가지 않는다.'

# 동시성 문제 해결
## race condition
    상황: 공유된 자원에 서로 다른 스레드가 동시에 접근할 수 있는 상황에서 발생하는 상황으로
    원인: 하나의 스레드의 작업이 끝나기 이전의 상태를 참조 하여, 발생한다.
    해결방법: 하나의 스레드만 접근 가능하도록 한다.
## synchronized로 해결해보기:
    Item.removeStock을 synchronized 함수로 선언한다.
    추가 조건: spring's @Transactional annotation은 매 요청마다 새로운 Item 클래스를 만든다.
    따라서 진행시 어노테이션을 제거 해주어야한다.
    
    한계: synchronized는 하나의 프로세스 안에서만 동작한다. 이때 서버가 여러개 라면 위와 같은
    race condition현상이 발생한다.