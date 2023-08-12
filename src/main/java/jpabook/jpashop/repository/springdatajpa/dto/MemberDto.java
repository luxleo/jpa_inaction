package jpabook.jpashop.repository.springdatajpa.dto;

import lombok.Data;

@Data
public class MemberDto {
    private String name;
    private int age;
    private Long id;

    public MemberDto(String name, int age, Long id) {
        this.name = name;
        this.age = age;
        this.id = id;
    }
}
