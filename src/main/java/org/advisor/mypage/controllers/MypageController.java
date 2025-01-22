package org.advisor.mypage.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/quit")
    public String quit(@PathVariable("seq") long seq){
    return null;
    }
    @PostMapping("/quit_ps")
    public String quit_ps(@PathVariable("seq") long seq){
        return null;
    }
}
