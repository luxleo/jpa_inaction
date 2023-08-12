package jpabook.jpashop.repository.springdatajpa;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.springdatajpa.dto.MemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MembersRepository extends JpaRepository<Member, Long> {
    List<Member> findByNameAndAgeGreaterThan(String name, int age);

    @Query("select m from Member m where m.name = :name and m.age > :age")
    List<Member> findUser(@Param("name") String name, @Param("age") int age);

    @Query("select m.name from Member m")
    List<String> findUserName();

    @Query("select new jpabook.jpashop.repository.springdatajpa.dto.MemberDto(m.name,m.age,m.id) from Member m")
    List<MemberDto> findMemberAsDto();

    @Query("select m from Member m where m.name in :names")
    List<Member> findUserByNameIn(@Param("names") List<String> names);

    // 다양한 반환타입 지정
    //List<Member> findByUserName(String name);

    //Member findByUserName(String name); // 단건 조회
    //Optional<Member> findByUserName(String name); // optional 조회
    Page<Member> findByAge(int age, Pageable pageable);

    // 만일 조인등이 들어가는 쿼리인 경우 count쿼리를 따로 작성한다.
    @Query(value = "select m from Member m left join m.team", countQuery = "select count(m.id) from Member m")
    Page<Member> findByUserName(String name, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age+1 where m.age>:age")
    int sample_updateMemberAge(int age);

    //@EntityGraph
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @Query("select m from Member m join fetch m.team")
    List<Member> manualJoinFetch();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m ")
    List<Member> annotJoinFetch();

    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByName(@Param("name") String name);
}
