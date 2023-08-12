package jpabook.jpashop.repository.springdatajpa;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Team;
import jpabook.jpashop.repository.springdatajpa.dto.MemberDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
//@Rollback(value = false)
class MembersRepositoryTest {
    @Autowired
    MembersRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;

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
        Member m1 = new Member("m1", null, null, 10);
        Member m2 = new Member("m2", null, null, 10);

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

    @Test
    void method_query() {
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
        memberRepository.save(m1);
        memberRepository.save(m2);
        memberRepository.save(m3);
        List<Member> members = memberRepository.findByNameAndAgeGreaterThan("m1", 10);
        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("repository내에서 바로 jpql 쿼리를 정의 할 수 있다.")
    void unnamed_query() {
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
        memberRepository.save(m1);
        memberRepository.save(m2);
        memberRepository.save(m3);
        List<Member> members = memberRepository.findUser("m1", 10);
        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("값 타입을 어떻게 조회할 것인가?")
    void value_type() {
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
        memberRepository.save(m1);
        memberRepository.save(m2);
        memberRepository.save(m3);
        List<String> usernames = memberRepository.findUserName();
        for (String username : usernames) {
            System.out.println("username = " + username);
        }
        List<MemberDto> memberAsDto = memberRepository.findMemberAsDto();
        for (MemberDto memberDto : memberAsDto) {
            System.out.println("memberDto.getName() = " + memberDto.getName());

        }
    }

    @Test
    @DisplayName("파라미터 바인딩은 Collection type도 가능하다.")
    void paramter_binding_test() {
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
        memberRepository.save(m1);
        memberRepository.save(m2);
        memberRepository.save(m3);

        List<Member> findmembers = memberRepository.findUserByNameIn(List.of("m1", "m2"));
        assertThat(findmembers.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("spring data jpa의 paging기능을 보자 - [List,Slice,Page]")
    void paging() {
        //given
        Member m1 = Member.builder()
                .name("m1")
                .age(10)
                .build();
        Member m2 = Member.builder()
                .name("m2")
                .age(10)
                .build();
        Member m3 = Member.builder()
                .name("m3")
                .age(10)
                .build();
        Member m4 = Member.builder()
                .name("m4")
                .age(10)
                .build();
        Member m5 = Member.builder()
                .name("m5")
                .age(10)
                .build();
        Member m6 = Member.builder()
                .name("m6")
                .age(10)
                .build();
        Member m7 = Member.builder()
                .name("m7")
                .age(10)
                .build();
        memberRepository.save(m1);
        memberRepository.save(m2);
        memberRepository.save(m3);
        memberRepository.save(m4);
        memberRepository.save(m5);
        memberRepository.save(m6);
        memberRepository.save(m7);

        //when
        // 주의할점: spring data jpa의 페이지는 0부터 시작한다.
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "name"));

        Page<Member> page = memberRepository.findByAge(10, pageRequest);
        // 반활할때는 이런식으로
        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getName(), m.getAge(), m.getId()));

        List<Member> content = page.getContent();

        assertThat(content.size()).isEqualTo(3); // pageRequest의 size와 같다.
        assertThat(page.getTotalElements()).isEqualTo(7L);
        assertThat(page.getNumber()).isEqualTo(0); // current slice(현재 참조하는 페이지) 반환
        assertThat(page.getTotalPages()).isEqualTo(3); // 모든 엔티티를 페이징 처리한 페이지 수 반환 -> 이 경우 3개 == 7/3 +1
        assertThat(page.isFirst()).isTrue(); // 일단 첫번째 반환하니까
        assertThat(page.hasNext()).isTrue(); // 현재 1 페이지 이므로
    }

    @Test
    @DisplayName("bulk 연산시 바로 db로 가기때문에 , em.flush()&&em.clear()로 하거나 @Modifing(clear=true)")
    void bulk_test() {
        Member m1 = Member.builder()
                .name("m1")
                .age(10)
                .build();
        Member m2 = Member.builder()
                .name("m2")
                .age(10)
                .build();
        Member m3 = Member.builder()
                .name("m3")
                .age(10)
                .build();
        Member m4 = Member.builder()
                .name("m4")
                .age(10)
                .build();
        Member m5 = Member.builder()
                .name("m5")
                .age(10)
                .build();
        Member m6 = Member.builder()
                .name("m6")
                .age(10)
                .build();
        Member m7 = Member.builder()
                .name("m7")
                .age(10)
                .build();
        memberRepository.save(m1);
        memberRepository.save(m2);
        memberRepository.save(m3);
        memberRepository.save(m4);
        memberRepository.save(m5);
        memberRepository.save(m6);
        memberRepository.save(m7);

        //when
        // 주의할점: spring data jpa의 페이지는 0부터 시작한다.
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "name"));

        Page<Member> page = memberRepository.findByAge(10, pageRequest);
        // 반활할때는 이런식으로
        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getName(), m.getAge(), m.getId()));

        List<Member> content = page.getContent();

        assertThat(content.size()).isEqualTo(3); // pageRequest의 size와 같다.
        assertThat(page.getTotalElements()).isEqualTo(7L);
        assertThat(page.getNumber()).isEqualTo(0); // current slice(현재 참조하는 페이지) 반환
        assertThat(page.getTotalPages()).isEqualTo(3); // 모든 엔티티를 페이징 처리한 페이지 수 반환 -> 이 경우 3개 == 7/3 +1
        assertThat(page.isFirst()).isTrue(); // 일단 첫번째 반환하니까
        assertThat(page.hasNext()).isTrue(); // 현재 1 페이지 이므로
    }

    @Test
    @DisplayName("간단한 fetch join은 @Entitygraph로")
    void entity_graph() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member m1 = Member.builder()
                .name("m1")
                .age(10)
                .team(teamA)
                .build();
        Member m2 = Member.builder()
                .name("m2")
                .age(10)
                .team(teamB)
                .build();
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> res = memberRepository.findAll();
        for (Member mem : res) {
            System.out.println("member = " + mem);
            if(mem.getTeam() == null) continue;
            System.out.println("mem.getTeam().getName() = " + mem.getTeam().getName());
        }
        List<Member> m11 = memberRepository.findEntityGraphByName("m1");
        for (Member member : m11) {
            System.out.println("member = " + member);
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }
    }

    @Test
    @DisplayName("BaseEntity지원")
    void base_entity() {
        Member memberA = Member.builder()
                .name("memberA")
                .age(10)
                .build();
        memberRepository.save(memberA);
        System.out.println("memberA = " + memberA);
        System.out.println("memberA.getCreatedDate() = " + memberA.getCreatedDate());
    }
}