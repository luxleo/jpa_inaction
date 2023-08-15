package jpabook.jpashop.repository.queryDsl;

import jpabook.jpashop.dto.queryDsl.MemberDto;
import jpabook.jpashop.repository.queryDsl.cond.QMemberSearchCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QMemberRepositoryCustom {
    List<MemberDto> searchWithCond(QMemberSearchCond searchCond);

    Page<MemberDto> searchWithCondAndPageSimple(QMemberSearchCond searchCond, Pageable pageable);
    Page<MemberDto> searchWithCondAndPageComplex(QMemberSearchCond searchCond, Pageable pageable);
}
