package org.example.AiSpringboot.DTO.Command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConsultationStreamDTO {

    @NotBlank(message = "sessionId不可为空")
    private String sessionId;

    @NotBlank(message = "初始消息不可为空")
    @Size(max=2000,message = "初始消息长度不可超过两千字符")
    private String userMassage;
}
