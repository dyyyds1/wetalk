package com.example.java_chatroom.controller;

import com.example.java_chatroom.dto.DeleteGroupTimeDTO;
import com.example.java_chatroom.mapper.GroupChatMapper;
import com.example.java_chatroom.mapper.MessageMapper;
import com.example.java_chatroom.mapper.MessageSessionMapper;
import com.example.java_chatroom.model.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
public class MessageSessionController {
    @Resource
    private MessageSessionMapper messageSessionMapper;

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private GroupChatMapper groupChatMapper;

    @GetMapping("/sessionList")
    @ResponseBody
    public Object getMessageSessionList(HttpServletRequest req){
        List<MessageSession> messageSessionList =new ArrayList<>();
        //获取到当前用户的userId（从spring的session中获取）
        HttpSession session=req.getSession(false);
        if (session == null){
            System.out.println("[getMessageSessionList] 获取会话失败！");
            return messageSessionList;
        }
        User user=(User) session.getAttribute("user");
        if (user == null){
            System.out.println("[getMessageSessionList] user==null ！");
            return messageSessionList;
        }
        //根据userId查询数据库，查出来有哪些会话
        List<Integer> sessionIdList=messageSessionMapper.getSessionIdByUserId(user.getUser_id());
        System.out.println("sessionIdList"+sessionIdList);
        for(int sessionId : sessionIdList) {
            //遍历会话Id，查询出每个会话里涉及到的好友都有谁
            MessageSession messageSession=new MessageSession();
            messageSession.setSessionId(sessionId);
            List<Friend> friends=messageSessionMapper.getFriendsBySessionId(sessionId, user.getUser_id());
            messageSession.setFriends(friends);

            //设置未读消息，每一个sessionId
            int count=messageSessionMapper.countOfNoRead(sessionId, user.getUser_id());
            messageSession.setCountNoRead(count);

            //遍历会话Id，查询出每个会话的最后一条信息
            String lastMessage = messageMapper.getLastMessageBySessionId(sessionId);

            int isGroup=groupChatMapper.countGroupChatBySessionId(sessionId);
            messageSession.setIsGroupChat(isGroup);
            //如果是群聊，获取群聊信息
            if (isGroup>0){
                GroupChat groupChat=groupChatMapper.getGroupChatBySessionId(sessionId);
                messageSession.setGroupChatName(groupChat.getGroupName());
                messageSession.setCreateBy(groupChat.getCreatedBy());
                int totalCount=messageSessionMapper.getGroupers(sessionId);
                int delCount=messageSessionMapper.getDelGroupers(sessionId);
                int curCount=totalCount-delCount;
                messageSession.setGroupersCount(curCount);
                int isDeleteGroup=groupChatMapper.isInGroupChat(sessionId, user.getUser_id());
                //删除了群聊，获取删除时间
                if (isDeleteGroup>0){
                    Date deleteTime=groupChatMapper.getDeleteGroupTime(sessionId,user.getUser_id());
                    DeleteGroupTimeDTO dto=new DeleteGroupTimeDTO();
                    dto.setDeleteTime(deleteTime);
                    dto.setSessionId(sessionId);
                    lastMessage=messageMapper.getDeleteLastMessageBySessionId(dto);
                }
            }


            //判断是否为空
            if (lastMessage ==null){
                messageSession.setLastMessage("");
            }else {
                //获取最后删除这个用户看到的会话列表的删除时间，如果没有就为空
                Date deleteLastTime=messageSessionMapper.getDeleteLastTime(sessionId, user.getUser_id());
                //获取最后一条消息的发送时间
                Date sessionLastTime=messageMapper.findLastTimeBySessionId(sessionId);
                if (deleteLastTime!=null) {
                    //如果不为空，并且最后一条消息的时间比删除时间要晚，就显示出来
                    int result = deleteLastTime.compareTo(sessionLastTime);
                    if (result <= 0) {
                        messageSession.setLastTime(sessionLastTime);
                        messageSession.setLastMessage(lastMessage);
                        messageSessionList.add(messageSession);
                    }
                }else {
                    //如果为空说明没有删除过，直接显示
                    messageSession.setLastTime(sessionLastTime);
                    messageSession.setLastMessage(lastMessage);
                    messageSessionList.add(messageSession);
                }

            }

        }
        Collections.sort(messageSessionList, new Comparator<MessageSession>() {
            @Override
            public int compare(MessageSession session1, MessageSession session2) {
                Date lastTime1 = session1.getLastTime();
                Date lastTime2 = session2.getLastTime();
                if (lastTime1 == null && lastTime2 == null) {
                    return 0;
                }
                if (lastTime1 == null) {
                    return 1;
                }
                if (lastTime2 == null) {
                    return -1;
                }
                return lastTime2.compareTo(lastTime1); // 降序排序
            }
        });
        System.out.println(messageSessionList);
        return messageSessionList;
    }

    @PostMapping("/session")
    @ResponseBody
    @Transactional
    public Object addMessageSession(int toUserId, @SessionAttribute("user") User user){
        HashMap<String,Integer> resp = new HashMap<>();
        //进行数据库message_session的插入操作,能够获取到会话的sessionId
        MessageSession messageSession=new MessageSession();
        messageSessionMapper.addMessageSession(messageSession);
        //给message_session_user表插入记录
        MessageSessionUserItem item1=new MessageSessionUserItem();
        item1.setSessionId(messageSession.getSessionId());
        item1.setUserId(user.getUser_id());
        messageSessionMapper.addMessageSessionUser(item1);
        //给message_session_user表插入记录
        MessageSessionUserItem item2=new MessageSessionUserItem();
        item2.setSessionId(messageSession.getSessionId());
        item2.setUserId(toUserId);
        messageSessionMapper.addMessageSessionUser(item2);
        System.out.println("[addMessageSession]新增会话成功！sessionId="+messageSession.getSessionId()
        +"u1= "+user.getUser_id()+" u2= "+toUserId);
        resp.put("sessionId", messageSession.getSessionId());

        return resp;
    }

    @PostMapping("/deleteSession")
    public void deleteSession(int sessionId, int userId){
        messageSessionMapper.markMessageSessionUserAsDeleted(sessionId,userId);
        int isHave=messageSessionMapper.isHaveDeleteData(sessionId,userId);
        //不是等于0就一定大于0
        if (isHave==0){
            messageSessionMapper.addDeleteData(sessionId,userId);
        }else{
            messageSessionMapper.updateDeleteData(sessionId,userId);
        }
    }
    //to
    @GetMapping("/getSessionId")
    public Object getSessionId(int userId,int friendId){
        MessageSession messageSession=new MessageSession();
        List<Integer> userSessionList=messageSessionMapper.getSessionIdsByUserId(userId);
        List<Integer> friendSessionList=messageSessionMapper.getSessionIdsByUserId(friendId);
        if (userSessionList.isEmpty()||friendSessionList.isEmpty()){
            return messageSession;
        }
        for (int i = 0; i < userSessionList.size(); i++) {
            for (int j = 0; j < friendSessionList.size(); j++) {
                if (userSessionList.get(i).equals(friendSessionList.get(j))){
                    messageSession.setSessionId(userSessionList.get(i));
                    return messageSession;
                }
            }
        }

        return messageSession;

    }

    @GetMapping("/getNoReadCount")
    public Object getNoReadCount(int sessionId,int userId){
        int count=messageSessionMapper.countOfNoRead(sessionId,userId);
        MessageSession messageSession=new MessageSession();
        messageSession.setCountNoRead(count);
        messageSession.setSessionId(sessionId);
        return messageSession;
    }


    @PostMapping("/readMessage")
    public Object readMessage(int sessionId,int userId){
        messageSessionMapper.setReadMessage(sessionId,userId);
        return "ok";
    }

}
