package org.advisor.admin.product.controller;

import lombok.RequiredArgsConstructor;
import org.advisor.global.rests.JSONData;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/admin/product ")
@RestController
@RequiredArgsConstructor
public class ProductAdminController {

    @PostMapping("/save")
    public JSONData save( Errors errors) {


        return null;
    }

    @GetMapping("/list")
    public JSONData list( ) {

        return null;
    }


    @GetMapping("/view/{seq}")
    public JSONData view(@PathVariable("seq") Long seq) {

        return null;
    }


    @DeleteMapping("/delete/{seq}")
    public JSONData delete(@PathVariable("seq") Long seq) {

        return null;
    }
    @DeleteMapping("/deletes")
    public JSONData deletes(@PathVariable("seq") Long seq) {

        return null;
    }
}
