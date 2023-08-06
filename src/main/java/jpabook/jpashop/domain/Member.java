package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String name;
    @Embedded
    private Address address;


    // mappedBy로 지명하면 해당 엔티티에서 수정이 일어나도 연관관계를 맺는 테이블은 수정이 일어나지 않는다.
    @OneToMany(mappedBy = "member") // mappedBy로 연관관계를 가지는 엔티티의 필드를 기재 하는 순간 -> 관계에 대한 소유권을 넘긴다
    private List<Order> orders = new ArrayList<>();

}
