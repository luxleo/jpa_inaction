package jpabook.jpashop.repository.springdatajpa;

import jpabook.jpashop.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team,Long> {
}
