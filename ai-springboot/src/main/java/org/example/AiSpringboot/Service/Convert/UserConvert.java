package org.example.AiSpringboot.Service.Convert;

import org.example.AiSpringboot.DTO.Command.UserRegisterCommonDTO;
import org.example.AiSpringboot.DTO.Response.UserLoginResponseDTO;
import org.example.AiSpringboot.Entity.User;
import org.example.AiSpringboot.enumClass.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserConvert {

    public static UserLoginResponseDTO.UserDetailResponseDTO entityToDetailResponse(User user){
        return UserLoginResponseDTO.UserDetailResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .gender(user.getGender())
                .genderDisplayName(getGenderDisplayName(user.getGender()))
                .status(user.getStatus())
                .birthday(user.getBirthday())
                .userType(user.getUserType())
                .userTypeDisplayName(user.getUserTypeDisplayName())
                .statusDisplayName(user.getStatusDisplayName())
                .displayName(user.getDisplayName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();


    }

    public static UserLoginResponseDTO entityToLoginResponse(String token,UserLoginResponseDTO.UserDetailResponseDTO userInfo){
        return UserLoginResponseDTO.builder()
                .userInfo(userInfo)
                .token(token)
                .roleType(userInfo.getUserType().toString())
                .build();
    }


    public static User registerCommandToEntity(UserRegisterCommonDTO commandDTO, String encodePassword){
        return  User.builder()
                .username(commandDTO.getUsername())
                .email(commandDTO.getEmail())
                .password(encodePassword)
                .nickname(commandDTO.getNickname())
                .phone(commandDTO.getPhone())
                .gender(commandDTO.getGender())
                .birthday(commandDTO.getBirthday())
                .userType(commandDTO.getUserType())
                .status(UserStatus.NORMAL.getCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }


    private static String getGenderDisplayName(Integer gender){
        if(gender==null){
            return "未知";
        }
        switch (gender){
            case 1:
                return "男";
            case 2:
                return "女";
            default:
                return"未知";
        }
    }


}
