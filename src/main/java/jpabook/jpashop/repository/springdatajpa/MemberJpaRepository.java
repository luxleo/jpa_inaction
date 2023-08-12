package jpabook.jpashop.repository.springdatajpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Getter @Setter // @Setter 의 경우 잘 사용하지는 않는다.
@Repository
public class MemberJpaRepository {
    @PersistenceContext
    private EntityManager em;

    public Member save(Member member) {
        em.persist(member);
        return member;
    }

    public void delete(Member member) {
        em.remove(member);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m",Member.class)
                .getResultList();
    }

    public Member find(Long id) {
        Member member = em.find(Member.class, id);
        return member;
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public long count() {
        return em.createQuery("select count(m) from Member m", Long.class).getSingleResult();
    }

    public List<Member> findByNameAndAgeGreaterThan(String name, int age) {
        return em.createQuery("select m from Member m where m.name = :name and m.age > :age order by m.age")
                .setParameter("name", name)
                .setParameter("age", age)
                .getResultList();
    }

    public List<Member> findAllWithPaging(int offset, int limit) {
        return em.createQuery("select m from Member m")
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public int sample_updateUserAge(int age) {
        return em.createQuery("update Member m set m.age = m.age+1 where m.age> :age ")
                .setParameter("age", age)
                .executeUpdate();
    }
}
