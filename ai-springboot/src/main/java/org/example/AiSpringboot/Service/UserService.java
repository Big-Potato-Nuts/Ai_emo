package org.example.AiSpringboot.Service;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.AiSpringboot.Common.Result;
import org.example.AiSpringboot.DTO.Command.UserLoginCommandDTO;
import org.example.AiSpringboot.DTO.Command.UserRegisterCommonDTO;
import org.example.AiSpringboot.DTO.Response.UserLoginResponseDTO;
import org.example.AiSpringboot.Entity.User;
import org.example.AiSpringboot.Exception.BusinessException;
import org.example.AiSpringboot.Mapper.UserMapper;
import org.example.AiSpringboot.Service.Convert.UserConvert;
import org.example.AiSpringboot.Util.JwtTokenUtil;
import org.example.AiSpringboot.enumClass.UserType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {


    @Resource
    private UserMapper userMapper;

    private  final  BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();

    public UserLoginResponseDTO login(UserLoginCommandDTO userLoginCommandDTO){

        // TODO 登录逻辑待实现
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUsername, userLoginCommandDTO.getUsername())
                .or()
                .eq(User::getEmail,userLoginCommandDTO.getUsername());

        User user = userMapper.selectOne(lambdaQueryWrapper);

        //验证用户密码
        if(user==null){
            throw new BusinessException("用户不存在");
        }
        String inputPassword = userLoginCommandDTO.getPassword().trim();

        if(!passwordEncoder.matches(inputPassword,user.getPassword())){
            throw new BusinessException("密码错误");
        }
        if(!user.isActive()){
            throw new BusinessException("用户被禁用");
        }

        //生成jwtToken
        String token = JwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());

        System.out.println(token);

        UserLoginResponseDTO.UserDetailResponseDTO userInfo = UserConvert.entityToDetailResponse(user);



        return UserConvert.entityToLoginResponse(token,userInfo);
    }

    public  UserLoginResponseDTO.UserDetailResponseDTO register(UserRegisterCommonDTO commonDTO){

        System.out.println(JSONUtil.parse(commonDTO));

        //验证密码是否正确
        if (!commonDTO.getPassword().equals(commonDTO.getConfirmPassword())) {
            throw new BusinessException("两次密码不一致");
        }

        //检查用户名是否存在
        LambdaQueryWrapper<User> userNameWrapper = new LambdaQueryWrapper<>();
        userNameWrapper.eq(User::getUsername, commonDTO.getUsername());
        if (userMapper.selectCount(userNameWrapper)>0){
            throw new BusinessException("用户名已存在");
        }

        //检查邮箱是否存在
        LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(User::getEmail, commonDTO.getEmail());
        if(userMapper.selectCount(emailWrapper)>0){
            throw  new BusinessException("邮箱存在");
        }

        if(!UserType.isValidCode(commonDTO.getUserType())){
            throw new BusinessException("无效的用户类型");
        }

        //创建用户
        String password = commonDTO.getPassword().trim();
        String encodePassword = passwordEncoder.encode(password);
        User user = UserConvert.registerCommandToEntity(commonDTO, encodePassword);

        userMapper.insert(user);
        return UserConvert.entityToDetailResponse(user);
    }



    public UserLoginResponseDTO.UserDetailResponseDTO getUserById(Long userId){
        User user = userMapper.selectById(userId);
        if(user==null){
            throw new BusinessException("用户不纯在");
        }
        return UserConvert.entityToDetailResponse(user);
    }



}
