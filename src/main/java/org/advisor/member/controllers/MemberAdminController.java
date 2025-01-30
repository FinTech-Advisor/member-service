package org.advisor.member.controllers;

import lombok.RequiredArgsConstructor;
import org.advisor.member.entities.Member;
import org.advisor.member.services.MemberUpdateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/member")
@RequiredArgsConstructor
public class MemberAdminController {

    private final MemberUpdateService memberUpdateService;


    /**
     * 1. 회원 목록 조회 (GET /admin/member/list)
     * 모든 회원을 조회하는 API
     */
    @GetMapping("/list")
    public ResponseEntity<List<Member>> getMemberList() {
        List<Member> members = memberUpdateService.getAllMembers();
        if (members.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 회원이 없으면 204 반환
        }
        return new ResponseEntity<>(members, HttpStatus.OK); // 회원 목록 반환
    }

    /**
     * 2. 회원 상세 조회 (GET /admin/member/view/{seq})
     * 특정 회원의 정보를 조회하는 API
     */
    @GetMapping("/view/{seq}")
    public ResponseEntity<Member> getMemberDetail(@PathVariable String seq) {
        Member member = memberUpdateService.getMemberById(seq);
        if (member == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 회원이 없으면 404 반환
        }
        return new ResponseEntity<>(member, HttpStatus.OK); // 회원 정보 반환
    }

    /**
     * 3. 회원 정보 수정 (POST /admin/member/save/{seq})
     * 특정 회원의 정보를 수정하는 API
     */
    @PostMapping("/save/{seq}")
    public ResponseEntity<Member> updateMember(@PathVariable String seq, @RequestBody Member member) {
        try {
            Member updatedMember = memberUpdateService.updateMemberProfile(seq, member.getName(), member.getEmail(), member.getPhone(),member.getPassword());
            return new ResponseEntity<>(updatedMember, HttpStatus.OK); // 수정된 회원 반환
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 회원이 없으면 404 반환
        }
    }

    /**
     * 4. 회원 강퇴 (POST /admin/member/quit/{seq})
     * 특정 회원을 강퇴(삭제)하는 API
     */
    @PostMapping("/quit/{seq}")
    public ResponseEntity<Void> quitMember(@PathVariable String seq) {
        try {
            memberUpdateService.deleteMember(seq); // 회원 강퇴
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 강퇴 완료, 204 반환
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 회원이 없으면 404 반환
        }
    }

    /**
     * 5. 회원 검색 (GET /admin/member/search)
     * 회원 이름이나 이메일을 기반으로 회원을 검색하는 API
     */
    @GetMapping("/search")
    public ResponseEntity<List<Member>> searchMembers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email) {

        List<Member> members = memberUpdateService.searchMembers(name, email);
        if (members.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 검색된 회원이 없으면 204 반환
        }
        return new ResponseEntity<>(members, HttpStatus.OK); // 검색된 회원 목록 반환
    }

    /**
     * 6. 회원 권한 수정 (POST /admin/member/role/{seq})
     * 특정 회원의 권한을 수정하는 API
     */
    @PostMapping("/role/{seq}")
    public ResponseEntity<Member> updateMemberRole(
            @PathVariable String seq,
            @RequestParam List<String> roles) { // roles는 리스트로 받기

        try {
            Member updatedMember = memberUpdateService.updateMemberRoles(seq, roles); // 여러 권한을 업데이트
            return new ResponseEntity<>(updatedMember, HttpStatus.OK); // 권한 수정된 회원 반환
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 회원이 없으면 404 반환
        }
    }

    /**
     * 7. 회원 비밀번호 변경 (POST /admin/member/password/{seq})
     * 특정 회원의 비밀번호를 변경하는 API
     */
    @PostMapping("/password/{seq}")
    public ResponseEntity<Void> changeMemberPassword(
            @PathVariable String seq,
            @RequestParam String newPassword) {

        try {
            memberUpdateService.changeMemberPassword(seq, newPassword); // 비밀번호 변경 처리
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 비밀번호 변경 완료, 204 반환
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 회원이 없으면 404 반환
        }
    }
}
