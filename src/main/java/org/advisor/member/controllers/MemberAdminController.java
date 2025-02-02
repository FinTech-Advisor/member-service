package org.advisor.member.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.advisor.member.entities.Member;
import org.advisor.member.services.MemberUpdateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/member")
@RequiredArgsConstructor
public class MemberAdminController {

    private final MemberUpdateService memberUpdateService;

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 권한 필요
    public ResponseEntity<List<Member>> getMemberList() {
        List<Member> members = memberUpdateService.getAllMembers();
        if (members == null || members.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(members);
    }

    @GetMapping("/view/{seq}")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 권한 필요
    public ResponseEntity<Member> getMemberDetail(@PathVariable String seq) {
        if (seq == null || seq.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Member member = memberUpdateService.getMemberById(seq);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(member);
    }

    @PostMapping("/save/{seq}")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 권한 필요
    public ResponseEntity<Member> updateMember(@PathVariable String seq, @RequestBody @Valid Member member) {
        if (seq == null || seq.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Member updatedMember = memberUpdateService.updateMemberProfile(
                    seq, member.getName(), member.getEmail(), member.getPhone(), member.getPassword());
            return ResponseEntity.ok(updatedMember);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/quit/{seq}")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 권한 필요
    public ResponseEntity<Member> quitMember(@PathVariable String seq) {
        if (seq == null || seq.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            memberUpdateService.deleteMember(seq); // 회원 탈퇴 처리 메서드 호출
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 권한 필요
    public ResponseEntity<List<Member>> searchMembers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email) {

        List<Member> members = memberUpdateService.searchMembers(name, email);
        if (members == null || members.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(members);
    }

    @PostMapping("/role/{seq}")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 권한 필요
    public ResponseEntity<Member> updateMemberRole(
            @PathVariable String seq,
            @RequestParam List<String> roles) {

        if (seq == null || seq.isBlank() || roles == null || roles.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Member updatedMember = memberUpdateService.updateMemberRoles(seq, roles);
            return ResponseEntity.ok(updatedMember);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/password/{seq}")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 권한 필요
    public ResponseEntity<Member> changeMemberPassword(
            @PathVariable String seq,
            @RequestParam String newPassword) {

        if (seq == null || seq.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Member updatedMember = memberUpdateService.changeMemberPassword(seq, newPassword);
            return ResponseEntity.ok(updatedMember);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
