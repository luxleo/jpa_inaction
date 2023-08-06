package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 철칙 1. 오가는 데이터는 절대 entity를 사용하지 않는다.
 */
@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    /**
     * entity를 그대로 반환할시에 엔티티의 변화가 전체 api스펙에 영향을 미친다.
     * 또한 전채 "count"와 같은 새로운 요구 사항이 있을때 대처가 어렵다.
     * 다양한 api의 요구사항을 만족시키는 것은 불가능에 수렴한다.
     */
    @GetMapping("/api/v1/members")
    public List<Member> memberList() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result membersResult() {
        List<Member> members = memberService.findMembers();
        List<MemberDto> collect = members.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result(collect, collect.size());
    }
    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
        private int count;
    }
    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
    }
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

    /**
     * 커맨드(update logic)와 쿼리(update된 멤버 조회 로직)를 분리하여 유지 보수성을 높여보자
     */
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable Long id,@RequestBody @Valid UpdateMemberRequest request
    ) {
        memberService.update(id, request.getName());
        Member updatedMember = memberService.findOne(id);
        return new UpdateMemberResponse(updatedMember.getId(), updatedMember.getName());
    }
    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse{
        private Long id;
        private String name;
    }
    @Data
    static class UpdateMemberRequest{
        private String name;
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
