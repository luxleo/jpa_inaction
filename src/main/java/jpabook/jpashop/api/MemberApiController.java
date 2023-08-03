package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 철칙 1. 오가는 데이터는 절대 entity를 사용하지 않는다.
 */
@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    /**
     * @param member
     * param에 entity 객체가 있다. entity는 수정이 잦을수 있는데
     * 그때 마다 api스펙이 바뀌는 것은 너무 유지보수가 힘들다.
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long joinId = memberService.join(member);
        return new CreateMemberResponse(joinId);
    }

    /**
     * @param request
     * param으로 dto를 따로 정해두면 entity의 수정이 컨트롤러에 side effect를 제공하지 않는다.
     * 또한 validation로직을 entity와 독립적으로 필요한 api에 따라 핏하게 설정할 수 있다.
     */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }
    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }
}
