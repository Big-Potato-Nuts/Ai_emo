package org.example.AiSpringboot.DTO.Command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConsultationSessionCreatDTO {


    @Size( max = 200,message = "会话标题不得超过两百字符")
    private String sessionTitle;

    @NotBlank(message = "初始消息不能为空")
    @Size(max=2000,message = "初始消息最多为两千个字符")
    private String initialMessage;




}
