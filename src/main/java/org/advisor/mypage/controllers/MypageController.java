package org.advisor.mypage.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {
@GetMapping("/")
    public String mypage(){
    return null;
}
@GetMapping("/profile")
    public String profile(){
return null;
}
    @PostMapping("/profile_ps")
    public String profile_ps(){
        return null;
    }
    @GetMapping("/myboard")
    public String myboard(){
    return null;
    }
}
