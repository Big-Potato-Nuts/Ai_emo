package org.example.AiSpringboot.Controller;


import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.example.AiSpringboot.Common.Result;
import org.example.AiSpringboot.DTO.Command.UserLoginCommandDTO;
import org.example.AiSpringboot.DTO.Command.UserRegisterCommonDTO;
import org.example.AiSpringboot.DTO.Response.UserLoginResponseDTO;
import org.example.AiSpringboot.Service.UserService;
import org.example.AiSpringboot.Util.JwtTokenUtil;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/user")
public class User {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result<UserLoginResponseDTO> Login(@Valid @RequestBody UserLoginCommandDTO commandDTO){

        UserLoginResponseDTO result=userService.login(commandDTO);
        return Result.ok(result);
    }

    @PostMapping("/add")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> register(@Valid @RequestBody UserRegisterCommonDTO commandDTO){


        UserLoginResponseDTO.UserDetailResponseDTO result=userService.register(commandDTO);
        return Result.ok(result);

    }

    //获取当前用户
    @GetMapping("/current")
    public  Result<UserLoginResponseDTO.UserDetailResponseDTO> getCurrentUser(){
        //从token中解析用户id
        String token = JwtTokenUtil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.getUserById(userId);



        return Result.ok(result);
    }

    //退出登录
    @PostMapping("/logout")
    public Result<Void> logout(){
        //JWT 为无状态认证：服务端不保存会话状态，退出登录只需返回成功，
        //由前端清除本地 token 与用户信息缓存即可完成登出
        return Result.ok();
    }

}
