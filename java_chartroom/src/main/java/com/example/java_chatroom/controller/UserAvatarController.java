package com.example.java_chatroom.controller;

import com.example.java_chatroom.mapper.UserAvatarMapper;
import com.example.java_chatroom.mapper.UserMapper;
import com.example.java_chatroom.model.User;
import com.example.java_chatroom.model.UserAvatar;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserAvatarController {

    @Autowired
    private UserAvatarMapper userAvatarMapper; // 自行定义 UserAvatarMapper 接口
    @Autowired
    private UserMapper userMapper;

    @PostMapping("/uploadAvatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile avatar,
                               @RequestParam("userId") int userId,
                               @RequestParam("username") String username) throws IOException {

        // 获取当前时间戳
        long currentTimeMillis = System.currentTimeMillis();
        // 生成随机参数（结合时间戳和随机数）
        String randomParam = currentTimeMillis + "_" + generateRandomNumber();

        // 上传文件目录，自行定义
//        String uploadDir = "......../resources/static/avatars/";
        // 获取文件扩展名
        String fileExtension = avatar.getOriginalFilename().substring(avatar.getOriginalFilename().lastIndexOf("."));
        // 构造新的文件名
        String newFilename = "avatar_" + randomParam + fileExtension;
        byte[] bytes = avatar.getBytes();
        String relativePath = "E:/icode/workplace(gitee)/wetalk/java_chartroom/src/main/resources/static/avatars/";

        Path targetFilePath = Paths.get(relativePath, newFilename);
        UserAvatar userAvatar = userAvatarMapper.getUserAvatarByUserId(userId);
        System.out.println("userAvatar  " + userAvatar);
        if (userAvatar != null) {
            //获取之前的保存的头像进行删除
            if (Files.exists(Paths.get(relativePath + userAvatar.getAvatar_path()))) {
                try {
                    Files.delete(Paths.get(relativePath + userAvatar.getAvatar_path()));
                } catch (IOException e) {
                    // 处理删除文件异常
                    e.printStackTrace();
                }
            }
            try {
                Files.write(targetFilePath, bytes);
            } catch (IOException e) {
                // 处理写入文件异常
                e.printStackTrace();
            }
            userAvatarMapper.updateAvatar(newFilename, userId);
            return "文件上传成功";
        }
        try {
            Files.write(targetFilePath, bytes);
        } catch (IOException e) {
            // 处理写入文件异常
            e.printStackTrace();
        }
        UserAvatar userAvatar1 = new UserAvatar();
        userAvatar1.setUserId(userId);
        userAvatar1.setUsername(username);
        userAvatar1.setAvatar_path(newFilename); // 存储文件路径
        //插入文件名字
        userAvatarMapper.insertUserAvatar(userAvatar1);
        return "文件上传成功";
    }

    @GetMapping("/getAvatar")
    public ResponseEntity<Map<String, String>> getAvatar(@Param("username") String username) {
        User user = userMapper.selectByName(username);
        int userId = user.getUser_id();
        String avatarPath = findAvatarPathByUsername(userId);
        Map<String, String> response = new HashMap<>();
        if (avatarPath != null) {
            response.put("avatarPath", "/avatars/" + avatarPath); // 使用相对路径
            return ResponseEntity.ok(response);
        } else {
            return null;
        }
    }

    private String findAvatarPathByUsername(int userId) {
        // 这里返回一个硬编码的头像路径，实际应从数据库或其他存储中获取
        UserAvatar userAvatar = userAvatarMapper.getUserAvatarByUserId(userId);
        if (userAvatar != null) {
            System.out.println(userAvatar.getAvatar_path());
            return userAvatar.getAvatar_path();
        }
        return null;
    }
    private int generateRandomNumber() {
        return (int) (Math.random() * 1000); // 生成一个0到99999之间的随机数
    }
}