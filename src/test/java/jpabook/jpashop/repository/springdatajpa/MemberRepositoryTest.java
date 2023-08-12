package jpabook.jpashop.repository.springdatajpa;

import jpabook.jpashop.domain.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MemberRepositoryTest {
    @Autowired
    MembersRepository memberRepository;
    @Test
    void saveMember() {
        Member memberA = Member.builder()
                .name("memberA")
                .build();
        memberRepository.save(memberA);
        Member findMember = memberRepository.findById(memberA.getId()).get();

        assertThat(memberA.getId()).isEqualTo(findMember.getId());
        assertThat(memberA.getName()).isEqualTo(findMember.getName());
    }

    @Test
    void basic_crud() {
        Member m1 = new Member("m1", null, null,10);
        Member m2 = new Member("m2", null, null,10);

        memberRepository.save(m1);
        memberRepository.save(m2);

        Member foundMember1 = memberRepository.findById(m1.getId()).get();
        Member foundMember2 = memberRepository.findById(m2.getId()).get();

        assertThat(foundMember1.getName()).isEqualTo(m1.getName());
        assertThat(foundMember2.getName()).isEqualTo(m2.getName());

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(4L);

        //전체 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(4);

        //삭제 검증
        memberRepository.delete(m1);
        long count1 = memberRepository.count();
        assertThat(count1).isEqualTo(3);

        //update 검증
        foundMember1.setName("changed!!");


    }
}