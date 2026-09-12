package org.example.AiSpringboot.enumClass;


import lombok.Getter;

@Getter
public enum UserType {

    USER(1,"普通用户"),
    ADMIN(2,"管理员");

    private final Integer code;
    private final String description;
    UserType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UserType fromCode(Integer code) {
        for (UserType userType : UserType.values()) {
            if (userType.code.equals(code)) {
                return userType;
            }
        }
        throw new IllegalArgumentException("未知的用户类型"+code);
    }

    public static boolean isValidCode(Integer code) {
        for (UserType userType : UserType.values()) {
            if (userType.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

}
