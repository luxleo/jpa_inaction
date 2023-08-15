package jpabook.jpashop.repository.queryDsl;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QMemberRepository extends JpaRepository<Member,Long>, QMemberRepositoryCustom {

}
