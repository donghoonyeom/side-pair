package sidepair.controller;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sidepair.common.interceptor.Authenticated;
import sidepair.common.resolver.MemberEmail;
import sidepair.member.application.MemberService;
import sidepair.member.configuration.request.MemberJoinRequest;
import sidepair.member.configuration.response.MemberInformationForPublicResponse;
import sidepair.member.configuration.response.MemberInformationResponse;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<Void> join(@RequestBody @Valid final MemberJoinRequest request) {
        final Long memberId = memberService.join(request);
        return ResponseEntity.created(URI.create("/api/members/" + memberId)).build();
    }

    @GetMapping("/me")
    @Authenticated
    public ResponseEntity<MemberInformationResponse> findMemberInformation(@MemberEmail final String email) {
        final MemberInformationResponse response = memberService.findMemberInformation(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{memberId}")
    @Authenticated
    public ResponseEntity<MemberInformationForPublicResponse> findMemberInfo(@PathVariable final Long memberId) {
        final MemberInformationForPublicResponse response = memberService.findMemberInformationForPublic(memberId);
        return ResponseEntity.ok(response);
    }
}
