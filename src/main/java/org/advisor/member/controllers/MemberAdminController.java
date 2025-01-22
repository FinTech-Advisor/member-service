package org.advisor.member.controllers;

import lombok.RequiredArgsConstructor;
import org.advisor.global.libs.Utils;
import org.advisor.global.rests.JSONData;
import org.advisor.member.entities.Member;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/admin/member")
@RestController
@RequiredArgsConstructor
public class MemberAdminController {
    private final Utils utils;



    @PostMapping("/save/{seq}")
    public JSONData save(@PathVariable("seq") Long seq, Errors errors) {


        return null;
    }

    @GetMapping("/list")
    public JSONData list(@ModelAttribute Member member) {

        return null;
    }


    @GetMapping("/view/{seq}")
    public JSONData view(@PathVariable("seq") Long seq) {

        return null;
    }


    @GetMapping("/quit/{seq}")
    public JSONData quit(@PathVariable("seq") Long seq) {

        return null;
    }
    @PostMapping("/quit_ps/{seq}")
    public JSONData quit_ps(@PathVariable("seq") Long seq) {

        return null;
    }
}
