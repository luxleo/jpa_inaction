package jpabook.jpashop.api.springdatajpa;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.springdatajpa.MembersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sdj")
public class SDJMemberApiController {
    private final MembersRepository membersRepository;

    @GetMapping("/members/{id}")
    public String getMember(@PathVariable Long id) {
        return membersRepository.findById(id).get().getName();
    }
    @GetMapping("/members/v2/{id}")
    public String getMemberV2(@PathVariable("id") Member member) {
        return member.getName();
    }
}
