package org.example.AiSpringboot.Controller;


import org.example.AiSpringboot.Common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Test {

    @GetMapping("/test")
    public Result<Object> test(){
        return Result.ok("hello world");
    }
}
