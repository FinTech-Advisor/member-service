package org.advisor.mypage.controllers;

import lombok.RequiredArgsConstructor;
import org.advisor.member.entities.Member;
import org.advisor.member.repositories.MemberRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {
    private final MemberRepository memberRepository;
@GetMapping("/")
    public String mypage(){
    return null;
}
@PostMapping("/profile")
    public String profile(){
return null;
}

    @GetMapping("/myboard")
    public String myboard(){
    return null;
    }
    @PostMapping("/quit")
    public String quit(@PathVariable("seq") long seq){
        Optional<Member> userOptional = memberRepository.findBySeq(seq);

        if (userOptional.isPresent()) {
            Member user = userOptional.get();

            // 추가적인 탈퇴 전 처리 (예: 사용자 상태 업데이트, 로그 기록 등)
            // user.setStatus("탈퇴"); // 예시로 상태 변경

            // 사용자 삭제
            /*      userRepository.delete(user);*/

            // 탈퇴 완료 메시지 반환
//            return JSONData.success("회원 탈퇴가 완료되었습니다.");
        } else {
            // 사용자가 존재하지 않으면 오류 메시지 반환
            /*        return JSONData.error("해당 사용자가 존재하지 않습니다.");*/
        }
        return null;
    }

}
