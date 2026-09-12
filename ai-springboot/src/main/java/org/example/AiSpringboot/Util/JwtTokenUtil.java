package org.example.AiSpringboot.Util;

import ch.qos.logback.core.util.StringUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.example.AiSpringboot.Config.JwtConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
@Component
public class JwtTokenUtil implements ApplicationContextAware {

    private static final String ISSUER="XuGeWang";

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {

        JwtTokenUtil.applicationContext = applicationContext;

    }


    private static JwtConfig getJwtConfig(){
        return applicationContext.getBean(JwtConfig.class);
    }

    public static String generateToken(Long userId,String userName,Integer roleType){

        try {
            JwtConfig jwtConfig = getJwtConfig();

            Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());

            Date expiration = new Date(System.currentTimeMillis() + jwtConfig.getExpiration());

            return JWT.create()
                    .withClaim("userId", userId)
                    .withClaim("userName", userName)
                    .withClaim("roleType", roleType)
                    .withExpiresAt(expiration)
                    .withIssuedAt(new Date())
                    .withIssuer(ISSUER)
                    .sign(algorithm);
        } catch (Exception e) {
            throw new RuntimeException("生成token失败"+e);
        }
    }



    //提取token

    public static  String extractTokenFromRequest(HttpServletRequest request){
        if(request==null){
            return null;
        }

        String tokenHeader = request.getHeader("token");
        if(StringUtils.hasText(tokenHeader)){
            return tokenHeader;
        }

        return null;
    }

    public static String getCurrentToken( ){
        ServletRequestAttributes attributes=(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes!=null){
            HttpServletRequest request=attributes.getRequest();
            Object token = request.getAttribute("jwtToken");
            if(token!=null){
                return token.toString();
            }
            String headerToken=extractTokenFromRequest(request);
            return  headerToken;
        }
        return null;


    }














    //验证token
    public static TokenVerificationResult validateToken(String token){

        DecodedJWT jwt = verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        String userName = jwt.getClaim("userName").asString();
        Integer roleType = null;
        try {
            roleType=jwt.getClaim("roleType").asInt();
        }catch (Exception e){
            String roleTypeStr = jwt.getClaim("roleType").asString();
            if(StringUtils.hasText(roleTypeStr)){
                roleType=Integer.valueOf(roleTypeStr);
            }
        }

        if(userId!=null &&StringUtils.hasText(userName) && roleType!=null){
            return new TokenVerificationResult(userId,userName,roleType,true);
        }




        return null;

    }

    //验证token有效
    public static DecodedJWT verifyToken(String token){
        if(!StringUtils.hasText(token)){
            throw new JWTVerificationException("token无效");
        }
        //token解码
        JwtConfig jwtConfig = getJwtConfig();
        Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }


    @Getter
    public static class TokenVerificationResult {
        private final Long userId;
        private final String userName;
        private final Integer roleType;
        private final boolean valid;


        public TokenVerificationResult(Long userId, String userName, Integer roleType, boolean valid) {
            this.userId = userId;
            this.userName = userName;
            this.roleType = roleType;
            this.valid = valid;
        }

    }



}
