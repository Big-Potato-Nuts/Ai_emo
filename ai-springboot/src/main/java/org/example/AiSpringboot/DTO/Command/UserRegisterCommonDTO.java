package org.example.AiSpringboot.DTO.Command;


import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class UserRegisterCommonDTO {

//    {
//        "username": "string",`1
//            "email": "string",1
//            "nickname": "string",
//            "phone": "string",1
//            "password": "string",1
//            "confirmPassword": "string",1
//            "gender": 0,1
//            "userType": 01
//    }
    @NotBlank(message = "用户名不能为空")
    @Size(min=3,max=50,message = "用户名长度必须在3——50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",message = "只能包含数字字母下划线")
    private String username;
    @NotBlank

    private String password;

    @NotBlank
    @Size(min =  6, max = 50,message = "确认密码长度在6-50之间")
    private String confirmPassword;

    @NotBlank(message = "邮箱不可为空")
    @Email(message = "邮箱格式错误")
    @Size(max=100,message = "邮箱长度不可超过50")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$",message = "手机号格式错误")
    private String phone;

    private Integer gender;
    private Integer userType=1;

    @Size(max=50,message = "昵称不超过50个字符")
    private String nickname;

    private LocalDate birthday;

}
