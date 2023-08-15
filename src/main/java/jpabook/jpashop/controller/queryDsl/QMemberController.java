package jpabook.jpashop.controller.queryDsl;

import jpabook.jpashop.dto.queryDsl.MemberDto;
import jpabook.jpashop.repository.queryDsl.QMemberRepository;
import jpabook.jpashop.repository.queryDsl.cond.QMemberSearchCond;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/q")
public class QMemberController {
    private final QMemberRepository memberRepository;

    @GetMapping("/v2/members")
    public Page<MemberDto> searchMemberV2(QMemberSearchCond searchCond, Pageable pageable) {
        log.info("[V2 called]");
        return memberRepository.searchWithCondAndPageSimple(searchCond, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberDto> searchMemberV3(QMemberSearchCond searchCond, Pageable pageable) {
        log.info("[V3 called]");
        return memberRepository.searchWithCondAndPageComplex(searchCond, pageable);
    }
}
