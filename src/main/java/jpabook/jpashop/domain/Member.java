package jpabook.jpashop.domain;

import jakarta.persistence.*;
import jpabook.jpashop.repository.springdatajpa.BaseEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter // Setter 사용하는 방식 보다 생성자로 생성하도록 한다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) //spring 컨테이너가 proxy기술로 만들때 사용
@ToString(of = {"id", "name","age","createdDate","lastModifiedDate"})
@NamedQuery(
        name = "Member.findByUserName",
        query = "select m from Member m where name = :name"
)
public class Member extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "member_id") // 이렇게 정의 해주는게 좋다 관례상
    private Long id;
    private String name;
    private int age;

    @Embedded
    private Address address;

    // mappedBy로 지명하면 해당 엔티티에서 수정이 일어나도 연관관계를 맺는 테이블은 수정이 일어나지 않는다.
    @OneToMany(mappedBy = "member") // mappedBy로 연관관계를 가지는 엔티티의 필드를 기재 하는 순간 -> 관계에 대한 소유권을 넘긴다
    private List<Order> orders = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Builder
    public Member(String name, Address address, Team team, int age) {
        this.name = name;
        this.address = address;
        this.team = team;
        this.age = age;
    }

    public Member(String name, int age) {
        this.name = name;
        this.age = age;
    }
    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
