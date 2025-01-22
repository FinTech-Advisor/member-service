package org.advisor.mypage.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mypage/card")
@RequiredArgsConstructor
public class CardController {
@GetMapping("/listcard")
public String listCard(){
    return null;
}
    @GetMapping("/addcard")
    public String addCard(){
        return null;
    }
    @PostMapping("/addcard_ps")
    public String addCard_ps(){
        return null;
    }
    @DeleteMapping("/deletcard")
    public String deletCard(){
    return null;
    }
}
