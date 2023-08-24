package com.example.java_chatroom.controller;

import com.example.java_chatroom.dto.FriendshipAndContentDTO;
import com.example.java_chatroom.dto.GroupChatRequest;
import com.example.java_chatroom.dto.InviteFriendRequest;
import com.example.java_chatroom.dto.ShowUserDTO;
import com.example.java_chatroom.mapper.*;
import com.example.java_chatroom.model.*;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
public class GroupChatController {
    @Autowired
    private GroupChatMapper groupChatMapper;

    @Autowired
    private MessageSessionMapper messageSessionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAvatarController userAvatarController;

    @Autowired
    private FriendMapper friendMapper;

    @PostMapping("/createGroupChat")
    @ResponseBody
    public GroupChat createGroupChat(@RequestBody GroupChatRequest request, @SessionAttribute("user") User user) {
        List<Integer> friendIds = request.getFriendIds();
        String groupName = request.getGroupName();
        GroupChat groupChat=new GroupChat();
        friendIds.add(user.getUser_id());
        MessageSession messageSession=new MessageSession();
        //插入群聊，并获取sessionId
        messageSessionMapper.addMessageSession(messageSession);
        //把friendIds的所有id都插入session_user
        for (int userId : friendIds){
            addMessageSession(userId,messageSession);
            int count = groupChatMapper.countGroupChatBySessionId(messageSession.getSessionId());
            if (count>0){
                groupChatMapper.deleteGroupUser(messageSession.getSessionId(),userId);
            }
        }
        groupChat.setCreatedBy(user.getUser_id());
        groupChat.setSessionId(messageSession.getSessionId());
        groupChat.setGroupName(groupName);
        try {
            groupChatMapper.createGroupChat(groupChat);
            return groupChat;
        } catch (Exception e) {
            return null;
        }
    }

    public void addMessageSession(int userId, MessageSession messageSession){
        //给message_session_user表插入记录
        MessageSessionUserItem item=new MessageSessionUserItem();
        item.setSessionId(messageSession.getSessionId());
        item.setUserId(userId);
        messageSessionMapper.addMessageSessionUser(item);
    }

    @GetMapping("/isGroupChat")
    public Map<String, Boolean> checkGroupChat(@RequestParam Integer sessionId) {
        int count = groupChatMapper.countGroupChatBySessionId(sessionId);
        boolean isGroupChat = count > 0;
        Map<String, Boolean> result = new HashMap<>();
        result.put("isGroupChat", isGroupChat);
        return result;
    }

    @GetMapping("/isInGroupChat")
    public Map<String, Boolean> isInGroupChat(@RequestParam Integer sessionId,@SessionAttribute("user") User user) {
        int count = groupChatMapper.isInGroupChat(sessionId, user.getUser_id());
        boolean isInGroupChat = count == 0;
        Map<String, Boolean> result = new HashMap<>();
        result.put("isInGroupChat", isInGroupChat);
        return result;
    }

    @PostMapping("/exitGroup")
    public void exitGroup(Integer sessionId,@SessionAttribute("user") User user){
        int count = groupChatMapper.isInGroupChat(sessionId, user.getUser_id());
        if (count==0){
            groupChatMapper.exitGroup(sessionId, user.getUser_id());
        }
    }

    @GetMapping("/getDeleteGroupTime")
    public Map<String, Date> getDeleteGroupTime(int sessionId,int userId){
        //如果删了群聊，获取群聊删除时间
        int count = groupChatMapper.isInGroupChat(sessionId, userId);
        Map<String, Date> map=new HashMap<>();
        if (count>0) {
            Date deleteTime = groupChatMapper.getDeleteGroupTime(sessionId, userId);
            map.put("deleteTime",deleteTime);
        }
        return map;
    }

    @GetMapping("/showUsers")
    public List<ShowUserDTO> showUsers(int sessionId){
        List<ShowUserDTO> friends=new ArrayList<>();
        List<Integer> userIds=groupChatMapper.selectAllUsers(sessionId);
        for (int userId : userIds){
            int count = groupChatMapper.isInGroupChat(sessionId, userId);
            if (count==0) {
                String username = userMapper.selectUsernameById(userId);
                ShowUserDTO showUserDTO = new ShowUserDTO();
                showUserDTO.setUsername(username);
                showUserDTO.setUserId(userId);
                ResponseEntity<Map<String, String>> response = userAvatarController.getAvatar(username);
                if (response != null) {
                    Map<String, String> responseBody = response.getBody();
                    if (responseBody != null) {
                        showUserDTO.setAvatar_path(responseBody.get("avatarPath"));
                    }
                }
                friends.add(showUserDTO);
            }
        }
        return friends;
    }

    //邀请新的好友加入群聊
    @PostMapping ("/inviteFriend")
    @ResponseBody
    public Map<String,Integer> inviteFriend(@RequestBody InviteFriendRequest inviteFriendRequest){

        int sessionId=inviteFriendRequest.getSessionId();
        List<Integer> friendIds=inviteFriendRequest.getFriendIds();
        System.out.println("friendIds"+friendIds);
        for (int friendId:friendIds) {
            int count = groupChatMapper.isInGroupChat(sessionId, friendId);
            if (count > 0) {
                groupChatMapper.deleteGroupUser(sessionId, friendId);
                continue;
            }
            groupChatMapper.inviteFriend(sessionId, friendId);
        }
        Map<String,Integer> map=new HashMap<>();
        map.put("sessionId",sessionId);
        return map;
    }

    //搜索未加入群聊的用户
    @GetMapping("/searchNoInGroupFriend")
    public List<ShowUserDTO> searchNoInGroupFriend(int sessionId, @SessionAttribute("user") User user){
        List<Friend> friends=friendMapper.getFriendsByUserId(user.getUser_id());
        List<Integer> groupUsers=groupChatMapper.selectAllUsers(sessionId);
        List<ShowUserDTO> result=new ArrayList<>();
        for (int i = 0; i < friends.size(); i++) {
            boolean flag=false;
            for (int j = 0; j < groupUsers.size(); j++) {
                // 有可能有人加了又退了，1就是退了，0没退
                int count = groupChatMapper.isInGroupChat(sessionId, friends.get(i).getFriendId());
                if (groupUsers.get(j)==friends.get(i).getFriendId()&&count==0){
                    flag=true;
                    break;
                }
            }
            if (!flag){
                ShowUserDTO userDTO=new ShowUserDTO();
                userDTO.setUsername(friends.get(i).getFriendName());
                userDTO.setUserId(friends.get(i).getFriendId());
                String username=userMapper.selectUsernameById(friends.get(i).getFriendId());
                ResponseEntity<Map<String, String>> response = userAvatarController.getAvatar(username);
                if (response != null) {
                    Map<String, String> responseBody = response.getBody();
                    if (responseBody != null) {
                        userDTO.setAvatar_path(responseBody.get("avatarPath"));
                    }
                }
                result.add(userDTO);
            }
        }
        return result;
    }
}
