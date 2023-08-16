package com.example.java_chatroom.controller;

import com.example.java_chatroom.mapper.FriendshipMapper;
import com.example.java_chatroom.mapper.UserMapper;
import com.example.java_chatroom.model.User;

import com.example.java_chatroom.service.VerificationCodeCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.simp.user.UserRegistryMessageHandler;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class UserController {
    @Resource
    private UserMapper userMapper;
    @Resource
    private FriendshipMapper friendshipMapper;


    private final VerificationCodeCache verificationCodeCache;

    @Autowired
    public UserController(VerificationCodeCache verificationCodeCache) {
        this.verificationCodeCache = verificationCodeCache;
    }

    @PostMapping("/login")
    public Object login(String username, String password, HttpServletRequest req){
        User user=userMapper.selectByName(username);
        //如果能找到user对象就看看密码匹不匹配
        if (user==null||!user.getPassword().equals(password)){
            //登录失败
            System.out.println("登陆失败"+user);
            return new User();
        }
        //登录成功创建会话
        HttpSession session=req.getSession(true);
        session.setAttribute("user",user);
        user.setPassword("");
        return user;
    }
    @PostMapping("/register")
    public Object register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email,
                           @RequestParam String verificationCode) {

        // 首先验证验证码是否有效
        if (!verificationCodeCache.isCodeValid(email, verificationCode)) {
            return new User(); // 或者返回其他错误提示信息
        }

        User user = new User();
        try {
            Date currentTime = new Date();
            user.setUsername(username);
            user.setPassword(password);
            user.setCreated_at(currentTime);
            user.setUpdated_at(currentTime);

            int ret = userMapper.insert(user);
            System.out.println("注册 ret：" + ret);
            user.setPassword("");
        } catch (DuplicateKeyException e) {
            user = new User();
        }
        user.setPassword("");
        return user;
    }

    @GetMapping("/userInfo")
    public Object getUserInfo(HttpServletRequest req){
        //先从请求中获取到会话
        HttpSession session=req.getSession(false);
        if(session==null){
            System.out.println("[getUserInfo]当前获取不到session");
            return new User();
        }
        //从会话中获取到之前保存的对象
        User user=(User) session.getAttribute("user");
        if (user==null){
            System.out.println("[getUserInfo]当前获取不到user对象");
            return new User();
        }
        user.setPassword("");
        return user;
    }


}
