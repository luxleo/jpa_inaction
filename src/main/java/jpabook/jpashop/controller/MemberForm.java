package jpabook.jpashop.controller;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter @Setter
public class MemberForm {
    @NotEmpty(message="어허 이름이 비었구만유")
    private String name;
    private String city;
    private String street;
    private String zipcode;

}
