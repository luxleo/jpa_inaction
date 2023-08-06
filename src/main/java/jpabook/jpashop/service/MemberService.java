package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // for lazy loading and 'readOnly' default
public class MemberService {
    private final MemberRepository memberRepository;
    @Autowired // 요즘은 생략가능하다.
    public MemberService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }

    /**
     * 회원 가입
     */
    @Transactional
    public Long join(Member member) {
        validateDuplicatedMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicatedMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다");
        }
    }

    public List<Member> findMembers(){
        return memberRepository.findAll();
    }


    public Member findOne(Long memberId) {
        return memberRepository.find(memberId);
    }

    /**
     * 업데이트시에는 변경감지를 이용하자
     * em.flush(),로 모든 영속성 컨텍스트를 반영하고 비운다.
     * 커맨드와 쿼리를 분리하는 정책을 가지자.
     */
    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.find(id);
        member.setName(name);
    }

    // 회원 조회

}
