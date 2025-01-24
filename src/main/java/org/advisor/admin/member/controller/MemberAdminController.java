package org.advisor.admin.member.controller;

import lombok.RequiredArgsConstructor;
import org.advisor.global.libs.Utils;
import org.advisor.global.rests.JSONData;
import org.advisor.member.entities.Member;
import org.advisor.member.repositories.MemberRepository;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequestMapping("/admin/member")
@RestController
@RequiredArgsConstructor
public class MemberAdminController {
    private final Utils utils;
    private final MemberRepository memberRepository;


    @PostMapping("/save/{seq}")
    public JSONData save(@PathVariable("seq") Long seq, Errors errors, Member member) {

        if (errors.hasErrors()) {
            return null;
        }

        Optional<Member> existingEntity = memberRepository.findById(seq);

        if (existingEntity.isPresent()) {
            // 수정

            return null;
        } else {
            // 새로운 데이터 저장
            member.setSeq(seq); // seq를 설정
            memberRepository.save(member);
            return null;
        }
    }

    @GetMapping("/list")
    public JSONData list(@ModelAttribute Member member) {

        return null;
    }


    @GetMapping("/view/{seq}")
    public JSONData view(@PathVariable("seq") Long seq) {

        Optional<Member> userOptional = memberRepository.findBySeq(seq);

        if (userOptional.isPresent()) {
//            User user = userOptional.get();

            // 필요한 사용자 정보 반환
//            return JSONData.success(user);
        } else {
            // 사용자가 존재하지 않으면 오류 메시지 반환
//            return JSONData.error("해당 사용자를 찾을 수 없습니다.");
        }
        return null;
    }


    @PostMapping("/quit/{seq}")
    public JSONData quit(@PathVariable("seq") Long seq) {
        // 사용자 조회
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

