package jpabook.jpashop.dto.queryDsl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"username", "age"})
public class UserDto {
    private String username;
    private int age;
}
