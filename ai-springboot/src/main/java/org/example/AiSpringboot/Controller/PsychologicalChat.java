package org.example.AiSpringboot.Controller;


import cn.hutool.json.JSONUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.Valid;
import org.example.AiSpringboot.AiService.PsychologicSupperService;
import org.example.AiSpringboot.AiService.StructOutPut;
import org.example.AiSpringboot.Common.Result;
import org.example.AiSpringboot.Common.ResultCode;
import org.example.AiSpringboot.DTO.Command.ConsultationSessionCreatDTO;
import org.example.AiSpringboot.DTO.Command.ConsultationStreamDTO;
import org.example.AiSpringboot.Util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("api/psychological-chat")
public class PsychologicalChat {


    @Autowired
    private PsychologicSupperService psychologicSupperService;

    @PostMapping("/session/start")
    public Result<StructOutPut.StreamChatSession> startSession(@Valid @RequestBody ConsultationSessionCreatDTO creatDTO){

        //获取当前用户

        DecodedJWT jwt = JwtTokenUtil.verifyToken(JwtTokenUtil.getCurrentToken());
        Long userId = jwt.getClaim("userId").asLong();

        StructOutPut.StreamChatSession session = psychologicSupperService.startSession(userId, creatDTO);


        return Result.ok(session);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody ConsultationStreamDTO streamDTO) {
        DecodedJWT jwt = JwtTokenUtil.verifyToken(JwtTokenUtil.getCurrentToken());
        Long userId = jwt.getClaim("userId").asLong();

        if(userId==null){
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data(JSONUtil.toJsonStr(Result.error(ResultCode.UNAUTHORIZED.getCode(),ResultCode.UNAUTHORIZED.getMsg(), "用户未登录")))
                    .build());
        }





        return psychologicSupperService.streamPsychologicalChat(streamDTO.getSessionId(),streamDTO.getUserMassage())
                .map(Fragment->{
                    return ServerSentEvent.<String>builder()
                            .event("message")
                            .data(JSONUtil.toJsonStr(Result.ok(Map.of("content",Fragment,"type","normal"))))
                            .build();
                })
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("{}")
                        .build()
                ))
                .delayElements(Duration.ofMillis(50));
    }





}
