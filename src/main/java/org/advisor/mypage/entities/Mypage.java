package org.advisor.mypage.entities;

import lombok.Data;

@Data
public class Mypage {
    private String email;
    private String password;
    private String confirmPassword;
    private String name;
    private String phone;
}
