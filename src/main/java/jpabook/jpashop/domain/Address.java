package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable // 내장형 객체 정의
@Getter
public class Address {
    private String city;
    private String street;
    private String zipcode;

    protected  Address(){}

    // 생성자가 초기값을 지정함으로써 변경 불가능한 객체로 만들었다.
    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
