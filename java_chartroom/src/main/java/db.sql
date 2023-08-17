drop database if exists java_chatroom;
create database if not exists java_chatroom charset utf8;

use java_chatroom;

drop table if exists user;

-- 创建 User 表
CREATE TABLE user (
    user_id int PRIMARY KEY auto_increment,
    username VARCHAR(20) unique,
    password VARCHAR(16),
    created_at DATETIME,
    updated_at DATETIME
);

insert into user values (1,'lwy','123',now(),now());
insert into user values (2,'dy','123',now(),now());
insert into user values (3,'admin','123',now(),now());
insert into user values (4,'abc','123',now(),now());
insert into user values (5,'laowangba','123',now(),now());
drop table if exists friendship;
-- 创建 Friendship 表
CREATE TABLE friendship (
    friendship_id int PRIMARY KEY auto_increment,
    user_id int,
    friend_id int,
    status boolean,
    created_at DATETIME,
    updated_at DATETIME

);

insert into friendship values (1,1,2,false,now(),now());
insert into friendship values (2,3,4,false,now(),now());

drop table if exists friendshipContent;
-- 创建 friendshipContent 表
CREATE TABLE friendshipContent (
    friendshipContent_id int PRIMARY KEY auto_increment,
    user_Id int,-- user是申请发起者
    username varchar(20),
    friend_id int,-- friend是申请接收者
    content varchar(50)-- 加好友的备注
);


drop table if exists message_session;
create table message_session(
    sessionId int primary key auto_increment,
    -- 最后一次访问时间
    lastTime datetime
);

insert into message_session values(1,'2000-05-01 00:00:00');
insert into message_session values(2,'2000-06-01 00:00:00');

drop table if exists message_session_user;
create table message_session_user (
    sessionId int,
    userId int,
    isDeleted BOOLEAN DEFAULT FALSE
);

-- 一号会话里有张三和李四
insert into message_session_user values (1,2,false);
insert into message_session_user values (1,1,false);


drop table if exists message;
create table message(
    messageId int primary key auto_increment,
    fromId int,-- 哪个用户发送的
    sessionId int,-- 消息属于哪一个会话
    content varchar(2048),-- 正文
    postTime datetime -- 发送时间
);
insert into message values (1,1,1,'今晚吃啥','2000-05-01 00:00:00');
insert into message values (2,2,1,'随便','2000-05-01 05:00:00');

drop table if exists deleteTime;
create table deleteTime(
    deleteTimeId int primary key auto_increment,
    userId int, -- 用户Id
    sessionId int , -- 关联会话的Id
    lastTime datetime -- 删除时间
);


CREATE TABLE message_status (
    statusId INT PRIMARY KEY AUTO_INCREMENT,
    messageId INT NOT NULL,
    userId INT NOT NULL,
    isRead BOOLEAN NOT NULL DEFAULT FALSE
);

insert into message_status values (1,1,1,false);
insert into message_status values (2,2,1,false);


# SELECT COUNT(*) AS unreadCount
# FROM message m
#          JOIN message_status ms ON m.messageId = ms.messageId
# WHERE m.sessionId = 3 AND ms.userId = 4 AND ms.isRead = FALSE;

# UPDATE message_status
# SET isRead = true
# WHERE messageId IN (
#     SELECT temp.messageId
#     FROM (
#              SELECT m.messageId
#              FROM message m
#                       JOIN message_status ms ON m.messageId = ms.messageId
#              WHERE m.sessionId = 1 AND ms.userId = 1
#          ) AS temp
# );'
drop table if exists user_avatar;
CREATE TABLE user_avatar (
     user_avatar_id INT PRIMARY KEY AUTO_INCREMENT,
     userId INT,
     username VARCHAR(20) NOT NULL,
     avatar_path VARCHAR(200)
);

drop table if exists group_chat;
# 群聊表的设计
CREATE TABLE group_chat (
    groupId INT PRIMARY KEY AUTO_INCREMENT,
    groupName VARCHAR(16) NOT NULL,
    creationTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    createdBy INT, -- 外键引用用户表中的 userId，表示群主
    sessionId INT
);
