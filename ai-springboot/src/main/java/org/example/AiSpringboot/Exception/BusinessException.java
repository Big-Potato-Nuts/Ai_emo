package org.example.AiSpringboot.Exception;


import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String  code;
    private final String  message;
    private  final Object data;

    public BusinessException(String message) {
        super(message);
        this.message=message;
        this.data=null;
        this.code="BUSINESS_ERROR";
    }
}
