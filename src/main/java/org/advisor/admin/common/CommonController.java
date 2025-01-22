package org.advisor.admin.common;


import ch.qos.logback.core.model.Model;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.advisor.global.libs.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@Controller

@RequiredArgsConstructor
@RequestMapping("/admin/basic")
public class CommonController {

    ;
    private final HttpServletRequest request;

    private final Utils utils;

    @ModelAttribute("menuCode")
    public String menuCode() {
        return "basic";
    }

    /**
     * 사이트 기본 정보 설정
     *
     * @param model
     * @return
     */
    @GetMapping({"", "/siteConfig"})
    public String siteConfig(Model model) {
        commonProcess("siteConfig", model);



        return null;
    }

    /**
     * 사이트 기본 정보 설정 처리
     *
     * @param
     * @param model
     * @return
     */
    @PatchMapping("/siteConfig")
    public String siteConfigPs(Model model) {


        return null;
    }

    // 약관 관리 양식, 목록
    @GetMapping("/terms")
    public String terms() {

        return null;
    }

    // 약관 등록 처리
    @PostMapping("/terms")
    public String termsPs(Errors errors, Model model) {


        if (errors.hasErrors()) {
            return null;
        }



        return null;
    }

    @RequestMapping(path="/terms", method={RequestMethod.PATCH, RequestMethod.DELETE})
    public String updateTerms( Model model) {






        return null;
    }






    /**
     * 기본설정 공통 처리 부분
     *
     * @param mode
     * @param model
     */
    private void commonProcess(String mode, Model model) {

        mode = StringUtils.hasText(mode) ? mode : "siteConfig";
        String pageTitle = null;

       if(mode.equals("terms")) {
            pageTitle = "약관 관리";
        }


        pageTitle += " - 기본설정";

    }
}
