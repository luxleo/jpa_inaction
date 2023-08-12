package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional // test의 경우 기본적으로 @Rollback(true)로 설정되어 있어서 쿼리가 날라가지 않는다.
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Test
    public void 회원가입() throws Exception{
        //given
        Member member = Member.builder()
                .name("kim")
                .build();
        //when
        Long savedId = memberService.join(member);

        //then
        assertEquals(member, memberService.findOne(savedId));
    }
    @Test()
    public void 중복_회원_에러() throws Exception{
        //given
        Member member1 = Member.builder()
                .name("kim")
                .build();
        Member member2 = Member.builder()
                .name("kim")
                .build();
        //when
        memberService.join(member1);

        //then
        assertThrows(IllegalStateException.class, () -> memberService.join(member2));
    }
}