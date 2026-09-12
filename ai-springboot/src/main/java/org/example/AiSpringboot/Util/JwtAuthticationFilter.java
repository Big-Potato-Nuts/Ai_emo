package org.example.AiSpringboot.Util;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.AiSpringboot.Common.ResultCode;
import org.example.AiSpringboot.Config.SecurityConfig;
import org.example.AiSpringboot.DTO.Response.UserLoginResponseDTO;
import org.example.AiSpringboot.Service.UserService;
import org.example.AiSpringboot.enumClass.UserStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JwtAuthticationFilter extends OncePerRequestFilter {

    @Resource
    private UserService userService;


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String requestURI = request.getRequestURI();

        return SecurityConfig.isPublicPath(requestURI);
    }

    /**
     * 是否跳过异步分发（ASYNC dispatch）时的过滤
     * 说明：SSE 流式接口（如 /api/psychological-chat/stream）返回 Flux 后，Tomcat 会以
     *      ASYNC 方式再次分发请求；默认 OncePerRequestFilter 会跳过 async 分发，
     *      导致第二次分发时 SecurityContext 丢失、被 AnonymousAuthenticationFilter 匿名化，
     *      最终 AuthorizationFilter 返回 403。
     *      这里返回 false，让 JWT 过滤器在 async 分发时重新从请求头解析 token 并设置认证。
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        //获取请求URL和方法
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String token = JwtTokenUtil.extractTokenFromRequest(request);
        if(StringUtils.hasText(token)){
            JwtTokenUtil.TokenVerificationResult validationResult = JwtTokenUtil.validateToken(token);
            if(validationResult!=null && validationResult.isValid()){

                //查询用户信息
                UserLoginResponseDTO.UserDetailResponseDTO user = userService.getUserById(validationResult.getUserId());
                if(user!=null && UserStatus.NORMAL.getCode().equals(user.getStatus())){
                    //用户状态正常，继续查询
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + validationResult.getRoleType())
                    );

                    UsernamePasswordAuthenticationToken authcation = new UsernamePasswordAuthenticationToken(
                            validationResult.getUserName(), null, authorities
                    );

                    SecurityContextHolder.getContext().setAuthentication(authcation);


                    request.setAttribute("jwtToken", token);


                }else{
                    clearSecurityContext();
                    ResponseUtil.writeError(response,ResultCode.TOKEN_ACCESS_FORBIDDEN);
                }


            }else {
                clearSecurityContext();
                ResponseUtil.writeError(response,ResultCode.TOKEN_INVALID);
            }



        }else{
            //清理上下文
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.ACCESS_UNAUTHORIZED);
        }
        chain.doFilter(request,response);

    }

    private void clearSecurityContext(){
        SecurityContextHolder.clearContext();
    }


}
