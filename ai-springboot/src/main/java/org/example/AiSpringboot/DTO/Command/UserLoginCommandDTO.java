package org.example.AiSpringboot.DTO.Command;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UserLoginCommandDTO {
    @NotBlank(message = "用户名或邮箱不可为空")
    @Size(max=100,message = "用户名不能超过100")
    private String username;

    @NotBlank(message = "密码不可为空")
    @Size(max=50,min = 6,message = "密码长度为6——50个字符")
    private String password;

}
