package org.advisor.mypage.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mypage/bank")
@RequiredArgsConstructor
public class BankController {
    @GetMapping("/listac")
    public String listAc(){
        return null;
    }
    @GetMapping("/addac")
    public String addAc(){
        return null;
    }
    @PostMapping("/addac_ps")
    public String addAc_ps(){
        return null;
    }
    @DeleteMapping("/deletac")
    public String deletAc(){
        return null;
    }
}
