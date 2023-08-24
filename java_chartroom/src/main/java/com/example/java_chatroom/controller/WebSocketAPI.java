package com.example.java_chatroom.controller;

import com.example.java_chatroom.component.OnlineUserManager;
import com.example.java_chatroom.dto.MessageRequest;
import com.example.java_chatroom.dto.MessageResponse;
import com.example.java_chatroom.mapper.FriendshipContentMapper;
import com.example.java_chatroom.mapper.GroupChatMapper;
import com.example.java_chatroom.mapper.MessageMapper;
import com.example.java_chatroom.mapper.MessageSessionMapper;
import com.example.java_chatroom.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;

@Component
public class WebSocketAPI extends TextWebSocketHandler {
    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private MessageSessionMapper messageSessionMapper;
    @Autowired
    private FriendshipController friendshipController;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private FriendshipContentMapper friendshipContentMapper;

    @Autowired
    private GroupChatMapper groupChatMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("[WebSocketAPI] 连接成功!");
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            return;
        }
        // 把这个键值对给存起来
        onlineUserManager.online(user.getUser_id(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("[WebSocketAPI] 收到消息!" + message.toString());
        // 1. 先获取到当前用户的信息. 后续要进行消息转发啥的.
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            System.out.println("[WebSocketAPI] user == null! 未登录用户, 无法进行消息转发");
            return;
        }
        // 2. 针对请求进行解析. 把 json 格式的字符串, 转成一个 Java 中的对象
        MessageRequest req = objectMapper.readValue(message.getPayload(), MessageRequest.class);

        if (req.getType().equals("message")) {
            // 就进行消息转发
            transferMessage(user, req);
        } else if (req.getType().equals("applyFriendship")){
            applyFriendship(user,req);
        }else {
            System.out.println("[WebSocketAPI] req.type 有误! " + message.getPayload());
        }

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("[WebSocketAPI] 连接异常!" + exception.toString());

        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            return;
        }
        onlineUserManager.offline(user.getUser_id(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("[WebSocketAPI] 连接关闭!" + status.toString());

        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            return;
        }
        onlineUserManager.offline(user.getUser_id(), session);
    }

    // 通过这个方法来完成消息实际的转发工作.
    // 第一个参数就表示这个要转发的消息, 是从谁那来的.
    private void transferMessage(User fromUser, MessageRequest req) throws IOException {
        // 1. 先构造一个待转发的响应对象. MessageResponse
        MessageResponse resp = new MessageResponse();
        resp.setType("message"); // 这里不设置也行, 默认也就是 message
        resp.setFromId(fromUser.getUser_id());
        resp.setFromName(fromUser.getUsername());
        resp.setSessionId(req.getSessionId());
        resp.setContent(req.getContent());

        // 2. 根据请求中的 sessionId, 获取到这个 MessageSession 里都有哪些用户. 通过查询数据库就能够知道了.
        List<Friend> friends = messageSessionMapper.getFriendsBySessionId(req.getSessionId(), fromUser.getUser_id());
        // 此处注意!!! 上述数据库查询, 会把当前发消息的用户给排除掉. 而最终转发的时候, 则需要也把发送消息的人也发一次.
        // 把当前用户也添加到上述 List 里面
        Friend myself = new Friend();
        myself.setFriendId(fromUser.getUser_id());
        myself.setFriendName(fromUser.getUsername());
        friends.add(myself);
        // 3. 循环遍历上述的这个列表, 给列表中的每个用户都发一份响应消息
        //    注意: 这里除了给查询到的好友们发, 也要给自己也发一个. 方便实现在自己的客户端上显示自己发送的消息.
        //    注意: 一个会话中, 可能有多个用户(群聊). 虽然客户端是没有支持群聊的(前端写起来相对麻烦), 后端无论是 API 还是 数据库
        //          都是支持群聊的. 此处的转发逻辑也一样让它支持群聊.
        // 转发的消息, 还需要放到数据库里. 后续用户如果下线之后, 重新上线, 还可以通过历史消息的方式拿到之前的消息.
        //    需要往 message 表中写入一条记录.
        Message message = new Message();
        message.setFromId(fromUser.getUser_id());
        message.setSessionId(req.getSessionId());
        message.setContent(req.getContent());
        // 像自增主键, 还有时间这样的属性, 都可以让 SQL 在数据库中生成
        messageMapper.add(message);
        int messageId=messageMapper.selectMessageId(req.getSessionId(), fromUser.getUser_id());
        String respJson ="";
        int isGroupChat=groupChatMapper.countGroupChatBySessionId(resp.getSessionId());
        int totalCount=messageSessionMapper.getGroupers(resp.getSessionId());
        int delCount=messageSessionMapper.getDelGroupers(resp.getSessionId());
        int curCount=totalCount-delCount;
        resp.setGroupersCount(curCount);
        for (Friend friend : friends) {
            //看看该用户是否删除了群聊,0未删除，1删除了
            int isDeleteGroup=groupChatMapper.isInGroupChat(req.getSessionId(), friend.getFriendId());
            if (isDeleteGroup>0){
                continue;
            }
            // 知道了每个用户的 userId, 进一步的查询刚才准备好的 OnlineUserManager, 就知道了对应的 WebSocketSession
            // 从而进行发送消息
            WebSocketSession webSocketSession = onlineUserManager.getSession(friend.getFriendId());
            messageSessionMapper.markMessageSessionUserAsRestore(req.getSessionId(),fromUser.getUser_id());
            messageSessionMapper.markMessageSessionUserAsRestore(req.getSessionId(),friend.getFriendId());
            //添加一条消息未读,如果是自己，就不加
            if (friend.getFriendId() != fromUser.getUser_id()){
                messageSessionMapper.addNoReadMessage(messageId,friend.getFriendId());
            }else {
                messageSessionMapper.addReadMessage(messageId,friend.getFriendId());
            }

            int count=messageSessionMapper.countOfNoRead(req.getSessionId(), friend.getFriendId());
            resp.setNoReadCount(count);
            resp.setIsGroupChat(isGroupChat);
            if (isGroupChat>0){
                GroupChat group=groupChatMapper.getGroupChatBySessionId(req.getSessionId());
                resp.setGroupName(group.getGroupName());
            }
            if (webSocketSession == null) {
                // 如果该用户未在线, 则不发送.
                continue;
            }
            // 把这个 java 对象转成 json 格式字符串
            respJson = objectMapper.writeValueAsString(resp);
            webSocketSession.sendMessage(new TextMessage(respJson));
//            List<Integer> sessionIds1=messageSessionMapper.getSessionIdsByUserId(fromUser.getUser_id());
//            List<Integer> sessionIds2=messageSessionMapper.getSessionIdsByFriendId(friend.getFriendId());
//            for (int sessionId : sessionIds1){
//                for (int i = 0; i < sessionIds2.size(); i++) {
//                    if (sessionId==sessionIds2.get(i)){
//                        messageSessionMapper.markMessageSessionUserAsRestore(sessionId,fromUser.getUser_id());
//                        messageSessionMapper.markMessageSessionUserAsRestore(sessionId,friend.getFriendId());
//                    }
//                }
//            }
        }
        System.out.println("[transferMessage] respJson: " + respJson);

    }
    private void applyFriendship(User fromUser, MessageRequest req) throws IOException {
        int toUserId = req.getToUserId();
        String content = req.getContent();
        System.out.println(content);
        // 在这里执行添加好友的逻辑，例如将好友申请插入数据库
        Friendship friendship=new Friendship();
        friendship.setUser_id(fromUser.getUser_id());
        friendship.setFriend_id(req.getToUserId());
        friendship.setStatus(true);
        int pending=friendshipController.addFriendship(friendship);
        // 获取目标用户的 WebSocketSession
        WebSocketSession toUserSession = onlineUserManager.getSession(toUserId);
        WebSocketSession mySession = onlineUserManager.getSession(fromUser.getUser_id());

        if (pending==0){
            MessageResponse resp = new MessageResponse();
            resp.setType("applyFriendship");
            resp.setContent("已经申请过啦");
            String respJson = objectMapper.writeValueAsString(resp);
            mySession.sendMessage(new TextMessage(respJson));
            return;
        }

        if (toUserSession != null) {
            // 如果目标用户在线，则发送好友申请响应消息
            MessageResponse resp = new MessageResponse();
            resp.setType("applyFriendship");
            resp.setFromId(fromUser.getUser_id());
            resp.setFromName(fromUser.getUsername());
            resp.setContent(content);
            resp.setToUserId(req.getToUserId());
            String respJson = objectMapper.writeValueAsString(resp);
            toUserSession.sendMessage(new TextMessage(respJson));
        }

        MessageResponse resp = new MessageResponse();
        resp.setType("applyFriendship");
        resp.setFromId(fromUser.getUser_id());
        resp.setFromName(fromUser.getUsername());
        resp.setContent("成功发起好友申请");
        resp.setToUserId(req.getToUserId());
        String respJson = objectMapper.writeValueAsString(resp);
        mySession.sendMessage(new TextMessage(respJson));
        FriendshipContent friendshipContent=new FriendshipContent();
        friendshipContent.setUserId(fromUser.getUser_id());
        friendshipContent.setUsername(fromUser.getUsername());
        friendshipContent.setFriendId(req.getToUserId());
        friendshipContent.setContent(content);
        friendshipContentMapper.addContent(friendshipContent);
    }

    // 其他方法...

}
