package jpabook.jpashop.repository.springdatajpa;

import jpabook.jpashop.domain.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
//@EnableJpaRepositories(basePackages = "jpabook.jpashop.domain") //원래는 설졍해주어야하는데 부트가 해준다.
@Transactional // jpa는 기본적으로 persistence context에서 데이터 변경,생성을 하므로
@Rollback(value = false) // update 시 기존에는 rollback때문에 쿼리가 안나감
class MemberJpaRepositoryTest {
    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void saveMember() {
        Member memberA = Member.builder()
                .name("memberA")
                .build();
        memberJpaRepository.save(memberA);
        Member findMember = memberJpaRepository.find(memberA.getId());

        assertThat(memberA.getId()).isEqualTo(findMember.getId());
        assertThat(memberA.getName()).isEqualTo(findMember.getName());
    }

    @Test
    void basic_crud() {
        Member m1 = new Member("m1", null, null,10);
        Member m2 = new Member("m2", null, null,10);

        memberJpaRepository.save(m1);
        memberJpaRepository.save(m2);

        Member foundMember1 = memberJpaRepository.findById(m1.getId()).get();
        Member foundMember2 = memberJpaRepository.findById(m2.getId()).get();

        assertThat(foundMember1.getName()).isEqualTo(m1.getName());
        assertThat(foundMember2.getName()).isEqualTo(m2.getName());

        //카운트 검증
        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(4L);

        //전체 조회 검증
        List<Member> all = memberJpaRepository.findAll();
        assertThat(all.size()).isEqualTo(4);

        //삭제 검증
        memberJpaRepository.delete(m1);
        long count1 = memberJpaRepository.count();
        assertThat(count1).isEqualTo(3);

        //update 검증
        foundMember1.setName("changed!!");
    }

    @Test
    void custom_query() {
        Member m1 = Member.builder()
                .name("m1")
                .age(10)
                .build();
        Member m2 = Member.builder()
                .name("m2")
                .age(30)
                .build();
        Member m3 = Member.builder()
                .name("m1")
                .age(15)
                .build();
        memberJpaRepository.save(m1);
        memberJpaRepository.save(m2);
        memberJpaRepository.save(m3);
        List<Member> members = memberJpaRepository.findByNameAndAgeGreaterThan("m1", 10);
        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    void jpa_paging() {
        Member m1 = Member.builder()
                .name("m1")
                .age(10)
                .build();
        Member m2 = Member.builder()
                .name("m2")
                .age(30)
                .build();
        Member m3 = Member.builder()
                .name("m1")
                .age(15)
                .build();
        Member m4 = Member.builder()
                .name("m1")
                .age(10)
                .build();
        Member m5 = Member.builder()
                .name("m2")
                .age(30)
                .build();
        memberJpaRepository.save(m1);
        memberJpaRepository.save(m2);
        memberJpaRepository.save(m3);
        memberJpaRepository.save(m4);
        memberJpaRepository.save(m5);

        List<Member> allWithPaging = memberJpaRepository.findAllWithPaging(0, 3);
        assertThat(allWithPaging.size()).isEqualTo(3);
    }

    @Test
    void bulk_update() {
        Member m1 = Member.builder()
                .name("m1")
                .age(10)
                .build();
        Member m2 = Member.builder()
                .name("m2")
                .age(10)
                .build();
        Member m3 = Member.builder()
                .name("m1")
                .age(20)
                .build();
        Member m4 = Member.builder()
                .name("m1")
                .age(20)
                .build();
        Member m5 = Member.builder()
                .name("m2")
                .age(30)
                .build();
        memberJpaRepository.save(m1);
        memberJpaRepository.save(m2);
        memberJpaRepository.save(m3);
        memberJpaRepository.save(m4);
        memberJpaRepository.save(m5);

        int resultCnt = memberJpaRepository.sample_updateUserAge(10);
        assertThat(resultCnt).isEqualTo(3);
    }
}