//标签页的切换
let isSelectApply=0;
function initSwitchTab(){
    //先获取相关元素,标签页按钮，会话列表，好友列表
    let tabSession =document.querySelector('.tab .tab-session');
    let tabFriend =document.querySelector('.tab .tab-friend');
    let appTab = document.querySelector('.tab .tab-applyFriend');
    let searchResults = document.querySelector('.hide-right');
    let right = document.querySelector('.right');
    let right2= document.querySelector('.hide-right2');
    let logo=document.querySelector('#rightLogo');
    //querySelectorAll可以同时选中多个元素,得到的是数组
    //list[0]是会话列表,list[0]是好友列表
    let lists=document.querySelectorAll('.list');
    //针对按钮，注册点击事件
    tabSession.onclick= function (){
        tabSession.style.backgroundImage ='url(img/say.png)';
        tabFriend.style.backgroundImage ='url(img/friends.png)';
        if (isSelectApply===1){
            appTab.style.backgroundImage='url(img/nAddFriendTab.png)';
        }
        logo.style.display='block';
        right.style.display = 'none';
        searchResults.style.display = 'none';
        right2.style.display = 'none';
        lists[0].classList= 'list';
        lists[1].classList= 'list hide';
    }

    tabFriend.onclick= function (){
        tabSession.style.backgroundImage ='url(img/say2.png)';
        tabFriend.style.backgroundImage ='url(img/friends2.png)';
        if (isSelectApply===1){
            appTab.style.backgroundImage='url(img/nAddFriendTab.png)';
        }
        logo.style.display='block';
        right.style.display = 'none';
        searchResults.style.display = 'none';
        right2.style.display = 'none';
        //好友列表显示，会话列表隐藏
        lists[0].classList= 'list hide';
        lists[1].classList= 'list';
    }

    appTab.addEventListener('click', function() {
        tabFriend.style.backgroundImage ='url(img/friends.png)';
        tabSession.style.backgroundImage ='url(img/say2.png)';
        appTab.style.backgroundImage='url(img/hAddFriendTab2.png)';
        isSelectApply=1;
        clickFriendApply();
    });
}

initSwitchTab();


////////////////////////////////////////////////////
//从服务器获取到用户登录对象

//创建websocket实例
let websocket =new WebSocket("ws://localhost:8080/WebSocketMessage");
// let websocket =new WebSocket("ws://" + location.host + "/WebSocketMessage");


websocket.onopen = function (){
    console.log("WebSocket 连接成功! ");
}
websocket.onmessage = function (e){
    console.log("WebSocket 收到消息! "+ e.data);
    //此时收到的 e.data 是一个字符串，需要转成js对象
    let resp = JSON.parse(e.data);
    if (resp.type === 'message'){
        //处理消息响应
        handleMessage(resp);
    } else if (resp.type === 'applyFriendship'){
        handleApplyFriendship(resp);

    }else {
        console.log("resp.type 不符合要求！");

    }
}

function handleApplyFriendship(resp){
    let userDiv=document.querySelector('.main .left .user ');
    if (resp.toUserId==userDiv.getAttribute('user-id')){
        let friendRequestIcon = document.querySelector('.tab .tab-applyFriend')
        friendRequestIcon.style.backgroundImage='url(img/hAddFriendTab.png)';
        isSelectApply=0;
    }
}
//TODO
function handleMessage(resp){
    // 客户端收到消息，展示出来
    // 展示到对应的会话预览区域，以及右侧消息列表中
    // 1. 根据响应中的 sessionId 获取到当前会话对应的 li 标签
    let userDiv = document.querySelector('.main .left .user');
    let userId = userDiv.getAttribute("user-id");
    let right = document.querySelector('.right');
    let curSessionLi = findSessionLi(resp.sessionId);
    // 如果li标签不存在 就创建一个新的会话
    if (curSessionLi == null && resp.isGroupChat==1){
        let userAvatar = document.createElement('img');
        userAvatar.src="avatars/groupChat.jpg";
        //创建新会话
        curSessionLi = document.createElement('li');
        curSessionLi.setAttribute('message-session-id',resp.sessionId);
        //此处p标签放预览内容
        curSessionLi.innerHTML = '<h3>' + resp.groupName+' ('+resp.groupersCount+')' + '</h3>'
            + '<p></p>';
        curSessionLi.onclick = function (){
            clickSession(curSessionLi);
        }
        curSessionLi.appendChild(userAvatar);
    } else if (curSessionLi == null){
        let userAvatar = document.createElement('img');
        findUserAvatar(userAvatar, resp.fromName);
        //创建新会话
        curSessionLi = document.createElement('li');
        curSessionLi.setAttribute('message-session-id',resp.sessionId);
        //此处p标签放预览内容
        curSessionLi.innerHTML = '<h3>' + resp.fromName + '</h3>'
            + '<p></p>';
        curSessionLi.onclick = function (){
            clickSession(curSessionLi);
        }
        curSessionLi.appendChild(userAvatar);

    }
    // 2. 把新消息，显示到会话的预览区域
    let computedStyle = window.getComputedStyle(right);
    let p = curSessionLi.querySelector('p');
    p.innerHTML = resp.content;
    if (p.innerHTML.length > 10){
        p.innerHTML =p.innerHTML.substring(0,10)+'...';
    }
    // 3. 把收到消息的会话置顶
    let sessionListUL = document.querySelector('#session-list');
    // 4. 如果当前收到消息的会话处于被选中状态，则把当前的消息放到右侧消息列表中
    if (curSessionLi.className === 'selected' && computedStyle.display === 'block'){
        // 把消息列表中添加一个新消息
        updateIsRead(curSessionLi.getAttribute('message-session-id'),userId);
        let spanElement = curSessionLi.querySelector('span');
        if (spanElement) {
            // 如果li元素中有span，则隐藏它
            spanElement.style.display = 'none';
        }
        let messageShowDiv = document.querySelector('.right .message-show');
        addMessage(messageShowDiv,resp);
        scrollBottom(messageShowDiv);
    }else{
        if (resp.noReadCount>0){
            let spanElement = curSessionLi.querySelector('span');
            console.log(spanElement);
            console.log(resp.noReadCount);
            if (spanElement) {
                // 如果li元素中有span，则隐藏它
                if (resp.noReadCount > 99) {
                    spanElement.textContent = '99+';
                } else {
                    spanElement.textContent = resp.noReadCount;
                }
            }else {
                spanElement = document.createElement('span');
                if (resp.noReadCount > 99) {
                    spanElement.textContent = '99+';
                } else {
                    spanElement.textContent = resp.noReadCount;
                }
                curSessionLi.appendChild(spanElement);
            }
            spanElement.style.display = 'block';

        }
    }
    sessionListUL.insertBefore(curSessionLi,sessionListUL.children[0]);
    // 新增消息同时，调整滚动条位置
}

function updateIsRead(sessionId,userId){
    $.ajax({
        type: 'post',
        url: '/readMessage',
        data: {
            sessionId: sessionId,
            userId: userId
        },
        success: function (resp){
            if (resp==='ok'){

            }else {
                console.log('读取失败');
            }
        },
        error: function (){
            console.log('读取失败');
        }
    })
}
function findSessionLi(targetSessionId){
    //获取会话列表中li标签
    let sessionLis = document.querySelectorAll('#session-list li');
    for (let li of sessionLis){
        let sessionId = li.getAttribute('message-session-id');
        if (sessionId == targetSessionId){
            return li;
        }
    }
    //当前新用户对当前用户发信息，没有会话
    return null;
}

websocket.onclose = function (){
    console.log("WebSocket 连接关闭! ");
}

websocket.onerror = function (){
    console.log("WebSocket 连接异常! ");
}
/////实现消息发送、接收逻辑
function initSendButton(){
    //先获取到发送按钮
    let sendButton = document.querySelector('.right .ctrl button');
    let messageInput = document.querySelector('.right .message-input');

    //给发送按钮注册一个点击事件
    sendButton.onclick = async function (){
        // a) 先针对输入框的内容做个简单判断，比如输入框内容为空，则啥都不干
        let titleDiv = document.querySelector('.right .title');
        if (!messageInput.value){
            return;
        }
        // b) 获取当前选中li的sessionId
        let selectedLi = document.querySelector('#session-list .selected');
        if (selectedLi == null){
            // 当前li标签没有被选中
            return;
        }
        let sessionId = selectedLi.getAttribute('message-session-id');
        let friendId=titleDiv.getAttribute('friendId');
        let isFriend;
        if (friendId) {
            isFriend = await checkIfFriend();
        }
        let isGroup = await checkIfInGroup(sessionId);
        if (!isFriend && !isGroup && friendId) {
            // 如果不是好友，则不发送消息
            alert('你只能向好友发送消息！');
            return;
        }
        if (!isFriend && !isGroup && !friendId) {
            // 如果不是好友，则不发送消息
            alert('你只能向已经加入的群聊发送消息！');
            return;
        }
        // c) 构造 json 数据
        let req = {
            type: 'message',
            sessionId: sessionId,
            content: messageInput.value,

        };
        req=JSON.stringify(req);
        console.log("[websocket] send: " + req);
        // d) 通过websocket 发送消息
        websocket.send(req);
        // e) 发送完成清空输入框
        messageInput.value='';

    }
}

function checkIfInGroup(sessionId){
    return new Promise((resolve, reject) => {
        $.ajax({
            type: "get",
            url: "/isInGroupChat",
            data: {
                sessionId: sessionId
            },
            success: function (resp) {
                if (resp.isInGroupChat === true) {
                    resolve(true); // 使用resolve传递结果
                } else {
                    resolve(false); // 返回默认值false
                }
            },
            error: function () {
                reject(new Error("获取群聊关系失败!")); // 使用reject传递错误信息
            }
        })
    });

}
function checkIfFriend() {
    return new Promise((resolve, reject) => {
        let userDiv = document.querySelector('.main .left .user');
        let userId = userDiv.getAttribute("user-id");
        let titleDiv = document.querySelector('.right .title');
        let friendId = titleDiv.getAttribute('friendId');
        $.ajax({
            type: 'post',
            url: '/isFriend',
            data: {
                userId: userId,
                friendId: friendId,
            },
            success: function (resp) {
                if (resp === true) {
                    resolve(resp); // 使用resolve传递结果
                } else {
                    resolve(false); // 返回默认值false
                }
            },
            error: function () {
                reject(new Error("获取好友关系失败!")); // 使用reject传递错误信息
            }
        });
    });
}

initSendButton();

function getUserInfo(){
    $.ajax({
        type: 'get',
        url: 'userInfo',
        success: function (body){
            //从服务器获取到数据，校验结果是否有效，无效就跳转到登陆页面
            //有效就把用户名显示到界面上
            if(body.user_id && body.user_id>0){
                let userDiv=document.querySelector('.main .left .user ');
                userDiv.innerHTML=body.username;
                userDiv.setAttribute("user-id",body.user_id);


                initAvatar();
            }else {
                alert("当前用户未登录!");
                location.assign('/login.html');
            }

        }
    })
}

getUserInfo();
function initAvatar(){
    let userDiv=document.querySelector(".left .user");
    console.log(userDiv.textContent);
    let username=userDiv.textContent;
    $.ajax({
        type: "get",
        url: "/getAvatar",
        data:{
            username: username
        },
        success: function (body){
            if (body.avatarPath) {
                avatar.src = body.avatarPath;
            }else {
                avatar.src='../avatars/default-avatar.png';
            }
        },
        error: function (){
            console.log("出错了");
        }
    })
}
document.addEventListener('DOMContentLoaded', function() {
    const getFriend = document.getElementById('getFriend');
    const friendListContainer = document.getElementById('friend-list');
    let user_id;
    getFriend.addEventListener('click', function() {

        $.ajax({
            type: 'get',
            url: '/userInfo',
            success: function (body) {
                //从服务器获取到数据，校验结果是否有效，无效就跳转到登陆页面
                //有效就把用户名显示到界面上
                if (body.user_id && body.user_id > 0) {
                    user_id = body.user_id;
                    fetch(`/friend/friends?user_id=${user_id}`)
                        .then(response => response.json())
                        .then(data => {
                            renderFriendsList(data);
                        })
                        .catch(error => {
                            console.assert('请求好友列表数据失败!');
                            return;
                        });
                } else {
                    alert("当前用户未登录!");
                    location.assign('/login.html');
                }
            },
            error: function(){
                    console.log('获取好友列表失败！')
            }


        })

    });

    function renderFriendsList(friendsData) {
        friendListContainer.innerHTML = '';

        friendsData.forEach(function(friend) {
            const listItem = document.createElement('li');
            const h4 = document.createElement('h4');
            h4.textContent = friend.friendName;
            listItem.appendChild(h4);
            let userAvatar = document.createElement('img');
            findUserAvatar(userAvatar, friend.friendName);
            listItem.appendChild(userAvatar);
            listItem.classList.add('friend');
            listItem.setAttribute('friend-id',friend.friendId);
            friendListContainer.appendChild(listItem);
            //对每一个li标签加上点击事件
            listItem.onclick = function (){
                //参数部分表示用户点击的是哪一个好友
                clickFriend(friend);
            }
        });
    }
});



function getSessionList() {
    $.ajax({
        type: 'get',
        url: '/sessionList',
        success: function (body){
            //清空之前的会话列表
            let sessionListUL = document.querySelector('#session-list');
            sessionListUL.innerHTML='';
            //遍历数组构造页面
            for (let session of body){
                if(session.lastMessage.length>10){
                    session.lastMessage =session.lastMessage.substring(0,10)+'...';
                }
                let li = document.createElement('li');
                //把会话id保存到li标签的自定义属性中
                li.setAttribute('message-session-id',session.sessionId);
                if (session.isGroupChat==1){
                    li.setAttribute('createBy',session.createBy);
                }
                let userAvatar = document.createElement('img');
                if (session.isGroupChat==0) {
                    findUserAvatar(userAvatar, session.friends[0].friendName);
                }else {
                    userAvatar.src="avatars/groupChat.jpg";
                }
                li.appendChild(userAvatar);

                let h3 = document.createElement('h3');
                if (session.isGroupChat==0) {
                    h3.textContent = session.friends[0].friendName;
                }else {
                    h3.textContent = session.groupChatName+' ('+session.groupersCount+')';
                }
                li.appendChild(h3);

                let p = document.createElement('p');
                p.textContent = session.lastMessage;
                li.appendChild(p);

                if (session.countNoRead>0){
                    let countNoRead=document.createElement('span');
                    if (session.countNoRead>99) {
                        countNoRead.textContent = '99+';
                    }else {
                        countNoRead.textContent = session.countNoRead;
                    }
                    li.appendChild(countNoRead);
                }
                sessionListUL.appendChild(li);
                //给li新增点击事件
                li.onclick = function (){
                    //点击哪一个标签，此处对应的clickSession就能拿到哪个标签
                    let searchResults = document.querySelector('#hide-right');
                    let right = document.querySelector('#right');
                    let right2= document.querySelector('#hide-right2');
                    let logo=document.querySelector('#rightLogo');
                    logo.style.display='none';
                    right.style.display = 'block';
                    searchResults.style.display = 'none';
                    right2.style.display = 'none';
                    clickSession(li);
                }


            }
        }
    })
}
getSessionList();

function findUserAvatar(userAvatar,username){
    $.ajax({
        type: "get",
        url: "/getAvatar",
        data:{
            username: username
        },
        success: function (body){
            if (body.avatarPath) {
                userAvatar.src = body.avatarPath;
                console.log(body.avatarPath)
            }else {
                userAvatar.src='avatars/default-avatar.png';
            }
        },
        error: function (){
            console.log("出错了");

            userAvatar.src='avatars/default-avatar.png';
        }
    })
}

function isHaveApply(){
    $.ajax({
        type: 'post',
        url: '/searchApply',
        success: function (body){
            if (body.status!=null&&body.status===true){
                let friendRequestIcon = document.querySelector('.tab .tab-applyFriend')
                friendRequestIcon.style.backgroundImage='url(img/hAddFriendTab.png)';
                isSelectApply=0;
            }else {
                let friendRequestIcon = document.querySelector('.tab .tab-applyFriend')
                friendRequestIcon.style.backgroundImage='url(img/nAddFriendTab.png)';
            }
        }
    })
}
isHaveApply();
function clickSession(currentLi){
    let userDiv = document.querySelector('.main .left .user');
    let userId = userDiv.getAttribute("user-id");
    let searchResults = document.querySelector('.hide-right');
    let right = document.querySelector('.right');
    let right2 = document.querySelector('.hide-right2');
    let logo=document.querySelector('#rightLogo');
    logo.style.display='none';
    right.style.display = 'block';
    searchResults.style.display = 'none';
    right2.style.display = 'none';
    //1.设置高亮
    let allLis = document.querySelectorAll('#session-list>li');
    activeSession(allLis,currentLi);
    updateIsRead(currentLi.getAttribute('message-session-id'),userId);
    let spanElement = currentLi.querySelector('span');
    if (spanElement) {
        // 如果li元素中有span，则隐藏它
        spanElement.style.display = 'none';
    }
    let sessionId =currentLi.getAttribute('message-session-id');
    //2.获取指定会话历史消息
    if (sessionId>0) {
        judgeIsGroupChat(sessionId);
    }
}

function judgeIsGroupChat(sessionId){
    $.ajax({
        url: "/isGroupChat",
        method: 'GET',
        data:{
            sessionId: sessionId
        },
        success: function(response) {
            if (response.isGroupChat) {
                //是群聊，获取用户与群聊之间的历史消息
                getGroupHistory(sessionId);
            } else {
                //不是群聊，获取用户之间的历史消息
                getHistoryMessage(sessionId);
            }
        },
        error: function() {
            console.log('请求失败。');
        }
    });
}

function getGroupHistory(sessionId){
//console.log("获取历史消息 sessionId=" + sessionId);
    //先清空已有内容
    let titleDiv = document.querySelector('.right .title');
    titleDiv.innerHTML = '';
    let messageShowDiv = document.querySelector('.right .message-show');
    messageShowDiv.innerHTML = '';
    //重新设置会话标题
    //先找到当前选中的会话是哪一个，被选中的会话带有selected类
    let selectedH3 = document.querySelector('#session-list .selected>h3');
    if (selectedH3) {
        //不存在的情况，比如页面加载阶段，没有selected标签
        titleDiv.innerHTML = selectedH3.innerHTML;
        //console.log(selectedH3.innerHTML);
        let friendId=titleDiv.getAttribute("friendId");
        if (friendId!=null){
            titleDiv.removeAttribute("friendId");
        }

        let dropdownContainer = document.createElement('div');
        dropdownContainer.className = 'moreTab';


        // 创建下拉框内容区域的 div 元素
        let dropdownContent = document.createElement('div');
        dropdownContent.className = 'dropdown-content';
        dropdownContent.id = 'Dropdown';

        // 添加下拉框内容，这里使用了 3 个示例选项，可以根据需要添加更多选项
        let option1 = document.createElement('div');
        option1.textContent = '退出群聊';
        option1.className = 'exitGroup';
        option1.onclick = function () {
            exitGroup(sessionId);
        }
        dropdownContent.appendChild(option1);

        const option2 = document.createElement('div');
        option2.textContent = '删除聊天';
        option2.className = 'deleteChat';
        option2.onclick = function () {
            deleteChat(selectedH3.innerHTML);
        }
        dropdownContent.appendChild(option2);

        const option3 = document.createElement('div');
        option3.textContent = '全部用户';
        option3.className = 'showUsers';
        option3.onclick = function () {
            showUsers(sessionId);
        }
        dropdownContent.appendChild(option3);

        // 将下拉框内容区域添加到下拉框容器中
        dropdownContainer.appendChild(dropdownContent);

        // 为点击事件绑定 toggleDropdown 函数
        dropdownContainer.addEventListener('click', toggleDropdown);
        titleDiv.appendChild(dropdownContainer);
    }
    //发送ajax请求给服务器  获取该会话的历史消息
    let userDiv = document.querySelector('.main .left .user ');
    let userId = userDiv.getAttribute('user-id');
    $.ajax({
        type: 'get',
        url: 'message?sessionId=' + sessionId,
        success: function (body) {
            //body是js对象数组
            for (let message of body) {
                $.ajax({
                    type: 'get',
                    url: '/getDeleteDate',
                    data: {
                        sessionId: sessionId,
                        userId: userId,
                    },
                    success: function (t) {
                        getDeleteGroupTime(sessionId,userId,message,t);
                    },
                    error: function () {
                        console.assert("获取时间失败")
                    }
                })

            }

        }
    })
}

let x = document.querySelector('.left .showUsers .close');
x.onclick = function () {
    let showUsersGroup = document.querySelector('.left .showUsers');
    showUsersGroup.style.display = 'none';
}


//查看群用户
function showUsers(sessionId){
    let showUsersGroup = document.querySelector('.left .showUsers');
    showUsersGroup.style.display = 'block';
    $.ajax({
        type: 'get',
        url: '/showUsers',
        data: {
            sessionId: sessionId
        },
        success: function (friends){
            var userList = $('.userList'); // 获取用户列表容器

            // 清空用户列表容器，以便重新填充数据
            userList.empty();

            // 遍历朋友列表数据，并动态生成HTML
            friends.forEach(function (friend) {
                var userBox = $('<li class="userBox"></li>'); // 创建用户盒子
                var img = $('<img>').attr('src', friend.avatar_path || 'avatars/default-avatar.png'); // 设置头像图片
                var usernameDiv = $('<div class="usernameDiv"></div>').text(friend.username); // 设置用户名

                // 将头像图片和用户名添加到用户盒子中
                userBox.append(img, usernameDiv);

                // 将用户盒子添加到用户列表容器中
                userList.append(userBox);
            });
            let addButton=document.createElement('button') // 邀请好友按钮
            addButton.className='addGroupFriend';
            addButton.title='邀请好友';
            addButton.textContent='+';
            addButton.onclick=function () {
                showNoInGroupFriends(sessionId);
            }
            userList.append(addButton);

        },
        error: function (){

        }
    })
}

function showNoInGroupFriends(sessionId){
    let submit = document.querySelector('#showNoInG > div.choice > button.submit');

    submit.onclick=function() {
        let checkboxes = document.querySelectorAll('#showNoInG > div.noInGList > ul > li > input[type=checkbox]');
        let checkNames = document.querySelectorAll('#showNoInG > div.noInGList > ul > li > .nameDiv');

        let friendNames = [];
        let selectedFriendIds = [];
        //friendId
        checkboxes.forEach((checkbox, index) => {
            if (checkbox.checked) {
                friendNames.push(checkNames[index].textContent); // Corrected here
                selectedFriendIds.push(checkbox.parentNode.getAttribute('friend-id'));
            }
        });
        console.log(selectedFriendIds);
        let friendNamesContext = friendNames.join(','); // Simplified here



        if (selectedFriendIds.length === 0) {
            alert('你还没有选择好友！');
            return;
        }
        // 将 friendId 数组发送给后端
        fetch('/inviteFriend', {
            method: 'POST',
            body: JSON.stringify({
                sessionId: sessionId,
                friendIds: selectedFriendIds
            }),
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => response.json())
            .then(data=> {
                // 处理后端返回的数据
                let overlay2 = document.querySelector('#overlay2');
                overlay2.style.display = 'none';
                let showNoInG = document.querySelector('.left .showNoInG');
                showNoInG.style.display='none';
                let showUsers=document.querySelector('.left .showUsers');
                showUsers.style.display='none';
                let req = {
                    type: 'message',
                    sessionId: sessionId,
                    content: '我将'+friendNamesContext+'拉入群聊，大家一起聊天吧',

                };
                req = JSON.stringify(req);
                console.log("[websocket] send: " + req);
                // 通过websocket 发送消息
                websocket.send(req);

            })
            .catch(error => {
                console.error('Error:', error);
            });
    };
    let cancel = document.querySelector('#showNoInG > div.choice > button.cancel');
    cancel.onclick=function() {
        let overlay2 = document.querySelector('#overlay2');
        overlay2.style.display = 'none';
        let showNoInG = document.querySelector('.left .showNoInG');
        showNoInG.style.display='none';

    };
    $.ajax({
        type: 'get',
        url: '/searchNoInGroupFriend',
        data: {
            sessionId: sessionId
        },
        success: function (friendsData){
            renderNoInGList(friendsData);
        },
        error: function (){
            console.log('showNoInGroupFriends出现错误');
        }
    })
}

function renderNoInGList(friendsData) {
    let friendList = document.querySelector('.noInGList ul');
    friendList.innerHTML='';
    let showNoInG = document.querySelector('.left .showNoInG');
    showNoInG.style.display='block';
    let overlay2 = document.querySelector('#overlay2');
    overlay2.style.display='block';
    friendsData.forEach(function(friend) {
        let listItem = document.createElement('li');
        listItem.classList.add('friend');
        listItem.setAttribute('friend-id', friend.userId);

        // 添加头像图片
        let userAvatar = document.createElement('img');
        findUserAvatar(userAvatar, friend.username);
        listItem.appendChild(userAvatar);

        // 添加姓名
        let nameDiv = document.createElement('div');
        nameDiv.textContent = friend.username; // 设置姓名
        nameDiv.className='nameDiv';
        listItem.appendChild(nameDiv);

        // 添加复选框
        let checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        listItem.appendChild(checkbox);

        friendList.appendChild(listItem);
    });

}

function getDeleteGroupTime(sessionId,userId,message,t){
    let messageShowDiv = document.querySelector('.right .message-show');
    $.ajax({
        type: "get",
        url: "/getDeleteGroupTime",
        data: {
            sessionId: sessionId,
            userId: userId
        },
        success: function (body){
            if (body.deleteTime){
                //删除之后的消息不显示
                if(message.postTime<body.deleteTime){
                    if (message.postTime > t.lastTime) {
                        addMessage(messageShowDiv, message);
                        //滚动到最底部
                        scrollBottom(messageShowDiv);
                    }
                }
            }else {
                if (message.postTime > t.lastTime) {
                    addMessage(messageShowDiv, message);
                    //滚动到最底部
                    scrollBottom(messageShowDiv);
                }
            }
        },
        error: function (){

        }
    })
}
function activeSession(allLis,currentLi){
    for (let li of allLis){
        if (li==currentLi){
            li.className = 'selected';
        }else {
            li.className = '';
        }
    }
}
//获取历史消息
async function getHistoryMessage(sessionId){

    //console.log("获取历史消息 sessionId=" + sessionId);
    //先清空已有内容
    let titleDiv = document.querySelector('.right .title');
    titleDiv.innerHTML = '';
    let messageShowDiv = document.querySelector('.right .message-show');
    messageShowDiv.innerHTML = '';
    //重新设置会话标题
    //先找到当前选中的会话是哪一个，被选中的会话带有selected类
    let selectedH3 = document.querySelector('#session-list .selected>h3');
    if (selectedH3) {
        //不存在的情况，比如页面加载阶段，没有selected标签
        titleDiv.innerHTML = selectedH3.innerHTML;
        //console.log(selectedH3.innerHTML);
        try {
            const friendId = await getFriendId(selectedH3.innerHTML); // 使用await等待异步结果
            titleDiv.setAttribute('friendId',friendId);
        } catch (error) {
            console.error(error.message);
        }


        let dropdownContainer = document.createElement('div');
        dropdownContainer.className = 'moreTab';


        // 创建下拉框内容区域的 div 元素
        let dropdownContent = document.createElement('div');
        dropdownContent.className = 'dropdown-content';
        dropdownContent.id = 'Dropdown';

        // 添加下拉框内容，这里使用了 3 个示例选项，您可以根据需要添加更多选项
        let option1 = document.createElement('div');
        option1.textContent = '删除好友';
        option1.className = 'deleteFriend';
        option1.onclick = function () {
            deleteFriend(selectedH3.innerHTML);
        }
        dropdownContent.appendChild(option1);

        const option2 = document.createElement('div');
        option2.textContent = '删除聊天';
        option2.className = 'deleteChat';
        option2.onclick = function () {
            deleteChat(selectedH3.innerHTML);
        }
        dropdownContent.appendChild(option2);

        const option3 = document.createElement('div');
        option3.textContent = '加入群聊';
        option3.className = 'setGroupChat';
        option3.onclick = function () {
            setGroupChat();
        }
        dropdownContent.appendChild(option3);

        // 将下拉框内容区域添加到下拉框容器中
        dropdownContainer.appendChild(dropdownContent);

        // 为点击事件绑定 toggleDropdown 函数
        dropdownContainer.addEventListener('click', toggleDropdown);
        titleDiv.appendChild(dropdownContainer);
    }
    //发送ajax请求给服务器  获取该会话的历史消息
    let userDiv = document.querySelector('.main .left .user ');
    let userId = userDiv.getAttribute('user-id');
    $.ajax({
        type: 'get',
        url: 'message?sessionId=' + sessionId,
        success: function (body) {
            //body是js对象数组
            for (let message of body) {
                $.ajax({
                    type: 'get',
                    url: '/getDeleteDate',
                    data: {
                        sessionId: sessionId,
                        userId: userId,
                    },
                    success: function (t) {
                        if (message.postTime > t.lastTime) {
                            addMessage(messageShowDiv, message);
                            //滚动到最底部
                            scrollBottom(messageShowDiv);
                        }
                    },
                    error: function () {
                        console.assert("获取时间失败")
                    }
                })

            }

        }
    })

}

// 将getFriendId函数改造为返回Promise的异步函数
function getFriendId(friendName) {
    return new Promise((resolve, reject) => {
        $.ajax({
            type: 'get',
            url: 'getFriendId',
            data: {
                friendName: friendName,
            },
            success: function (friend) {
                if (friend.friendId > 0) {
                    resolve(friend.friendId); // 使用resolve返回friendId
                } else {
                    alert('出了点小问题噢');
                    reject(new Error('获取friendId失败')); // 使用reject返回错误信息
                }
            },
            error: function () {
                alert('出了点小问题噢');
                reject(new Error('获取friendId失败')); // 使用reject返回错误信息
            }
        });
    });
}


function deleteChat(){
    let userDiv=document.querySelector('.main .left .user ');
    let userId = userDiv.getAttribute('user-id');
    let selectedLi = document.querySelector('#session-list .selected');
    if (selectedLi == null){
        // 当前li标签没有被选中
        return;
    }
    let sessionId = selectedLi.getAttribute('message-session-id');
    $.ajax({
        type: 'post',
        url: '/deleteSession',
        data: {
            sessionId: sessionId,
            userId: userId,
        },
        success: function (){
            console.log('删除成功');
            let titleDiv = document.querySelector('.right .title');
            titleDiv.innerHTML='';
            let messageShowDiv = document.querySelector('.right .message-show');
            messageShowDiv.innerHTML = '';
            getSessionList();
        },
        error: function(){
            console.log('删除失败');
        }
    })
}

//删除好友
function deleteFriend(friendName){
    let userDiv=document.querySelector('.main .left .user ');
    let userId = userDiv.getAttribute('user-id');
    let selectedLi = document.querySelector('#session-list .selected');
    if (selectedLi == null){
        // 当前li标签没有被选中
        return;
    }
    let sessionId = selectedLi.getAttribute('message-session-id');
    $.ajax({
        type: 'post',
        url: '/deleteFriend',
        data: {
            username: friendName,
            userId: userId,
        },
        success: function (body){
            if (body.content==='Success'){
                $.ajax({
                    type: 'post',
                    url: '/deleteSession',
                    data: {
                        sessionId: sessionId,
                        userId: userId,
                    },
                    success: function (){
                        console.log('删除成功');
                        getSessionList();
                        let right = document.querySelector('.right');
                        let logo=document.querySelector('#rightLogo');
                        right.style.display='none';
                        logo.style.display='block';
                    },
                    error: function(){
                        console.log('删除失败');
                    }
                })
            }else {
                console.assert('删除好友失败！');
            }
        },
        error: function (){
            console.assert('删除好友失败！');
        }
    })
}
function toggleDropdown() {
    const dropdownContent = document.getElementById('Dropdown');
    if (dropdownContent.style.display === 'block') {
        dropdownContent.style.display = 'none';
    } else {
        dropdownContent.style.display = 'block';
    }
}
function scrollBottom(elem){
    //获取可视区域高度
    let clientHeight = elem.offsetHeight;
    //获取内容的总高度
    let scrollHeight = elem.scrollHeight;
    //进行滚动操作
    elem.scrollTo(0,scrollHeight-clientHeight);
}

function addMessage(messageShowDiv,message){
    //使用这个div表示一条消息
    let messageDiv=document.createElement('div');
    //针对是不是自己发的，判定消息是否靠左，否则靠右
    let selfUserName = document.querySelector('.left .user').innerHTML;
    if (selfUserName === message.fromName){
        //消息是自己发的，靠左
        messageDiv.className = 'message message-right';
    }else {
        //消息不是自己发的，靠右
        messageDiv.className = 'message message-left';
    }

    // 创建消息内容的div
    let messageContentDiv = document.createElement('div');
    messageContentDiv.className = 'box';

    // 创建显示用户名的h4元素
    let messageContentUsernameDiv = document.createElement('div');
    let usernameH4 = document.createElement('h4');
    usernameH4.textContent = message.fromName;
    messageContentUsernameDiv.appendChild(usernameH4)
    messageContentDiv.appendChild(messageContentUsernameDiv);

    // 去除消息文本内容两侧的空白字符
    let trimmedContent = message.content.trim();

    // 创建显示消息文本的p元素
    let messageText = document.createElement('p');

    messageText.textContent = trimmedContent;
    messageContentDiv.appendChild(messageText);

    // 将消息内容的div添加到消息的主div中
    messageDiv.appendChild(messageContentDiv);

    // 将整个消息的div添加到消息区域中
    messageShowDiv.appendChild(messageDiv);


    // 计算消息长度，并设置气泡的宽度
    let fontSize=14;
    let maxWidth = 300; // 最大宽度（单位：像素）
    let minWidth = 30; // 最小宽度（单位：像素）
    let width = calculateMixedBubbleWidth(message.content,fontSize,minWidth,maxWidth);
    messageContentDiv.style.width = width + 'px';

    messageShowDiv.appendChild(messageDiv);
}
function calculateMixedBubbleWidth(messageContent, fontSize, minWidth, maxWidth) {
    // 去除消息文本内容两侧的空白字符
    let trimmedContent = messageContent.trim();

    let width = 0;
    for (let i = 0; i < trimmedContent.length; i++) {
        // 判断当前字符是否为中文，是则按照设定的字体大小计算宽度，否则按照英文字符宽度计算
        let charWidth = /[\u4E00-\u9FA5]/.test(trimmedContent[i]) ? fontSize : 12;
        width += charWidth;
    }

    // 添加一些额外宽度，例如气泡的边框和内边距
    width += 30; // 假设添加了20像素的额外宽度

    // 设置最大和最小宽度限制
    width = Math.min(Math.max(width, minWidth), maxWidth);

    return width;
}


//插眼-点击好友列表项触发的好友事件

function clickFriend(friend) {
    let userDiv=document.querySelector('.main .left .user ');
    let userId = userDiv.getAttribute('user-id');
    let sessionLi = findSessionByName(friend.friendName);
    let sessionListUl = document.querySelector('#session-list');
    $.ajax({
        type: 'get',
        url: '/getSessionId',
        data: {
            userId: userId,
            friendId: friend.friendId
        },
        success: function (body) {
            // 同时把标签页切换到会话列表
            let tabSession = document.querySelector('.tab .tab-session');
            tabSession.click();
            if (body.sessionId) {

                if (sessionLi){
                    // 如果会话已存在，就把这个会话设置成选中状态，并且置顶
                    sessionListUl.insertBefore(sessionLi, sessionListUl.children[0]);
                    // 此处设置会话选中状态，获取历史消息，这两个功能在clickSession已经有了，直接调用clickSession即可
                    clickSession(sessionLi);
                }else {
                    sessionLi = document.createElement('li');
                    sessionLi.innerHTML = '<h3>' + friend.friendName + '</h3>' + '<p></p>';
                    // 把标签进行置顶
                    sessionListUl.insertBefore(sessionLi, sessionListUl.children[0]);
                    sessionLi.onclick = function () {
                        clickSession(sessionLi);
                    }
                    sessionLi.setAttribute('message-session-id',body.sessionId);
                    sessionLi.click();


                }
            } else {
                sessionLi = document.createElement('li');
                sessionLi.innerHTML = '<h3>' + friend.friendName + '</h3>' + '<p></p>';
                // 把标签进行置顶
                sessionListUl.insertBefore(sessionLi, sessionListUl.children[0]);
                sessionLi.onclick = function () {
                    clickSession(sessionLi);
                }

                // 发消息给服务器，新创建的会话啥样的，并等待createSession完成
                createSession(friend.friendId, sessionLi);
                sessionLi.click();

            }

            let userAvatar = document.createElement('img');
            findUserAvatar(userAvatar, friend.friendName);
            sessionLi.appendChild(userAvatar);

        },
        error: function () {
            console.log("出现错误");
        }
    })

}

// function clickFriend(friend){
//     //先判定好友是否存在相应的会话
//     let sessionLi=findSessionByName(friend.friendName);
//     let sessionListUl =document.querySelector('#session-list');
//     if (sessionLi){
//         //如果存在匹配的会话结果，就把这个会话设置成选中状态，并且置顶
//         sessionListUl.insertBefore(sessionLi,sessionListUl.children[0]);
//         //此处设置会话选中状态，获取历史消息，这两个功能在clickSession已经有了，此处直接调用clickSession即可
//         clickSession(sessionLi);
//     }else {
//         //如果不存在，创建新会话
//         sessionLi = document.createElement('li');
//         sessionLi.innerHTML = '<h3>'+ friend.friendName+'</h3>'+'<p></p>';
//         //把标签进行置顶
//         sessionListUl.insertBefore(sessionLi,sessionListUl.children[0]);
//         sessionLi.onclick = function (){
//             clickSession(sessionLi);
//         }
//
//         //发消息给服务器，新创建的会话啥样的
//         createSession(friend.friendId,sessionLi);
//         sessionLi.click();
//
//
//     }
//
//     let searchResults = document.querySelector('.hide-right');
//     let right = document.querySelector('.right');
//     let right2= document.querySelector('.hide-right2');
//     right.style.display = 'block';
//     searchResults.style.display = 'none';
//     right2.style.display = 'none';
//
//     //同时把标签页切换到会话列表
//     let tabSession = document.querySelector('.tab .tab-session');
//     tabSession.click();
// }
// function createSession(friendId,sessionLi){
//     $.ajax({
//         type: 'post',
//         url: 'session?toUserId=' + friendId,
//         success: function (body){
//             console.log("会话创建成功！sessionId = "+body.sessionId);
//             sessionLi.setAttribute('message-session-id',body.sessionId);
//
//             console.log(sessionLi.getAttribute('message-session-id'));
//         },
//         error: function (){
//             console.log("会话创建失败！");
//         }
//     })
// }

function createSession(friendId, sessionLi) {
    return new Promise(function(resolve, reject) {
        $.ajax({
            type: 'post',
            url: 'session?toUserId=' + friendId,
            success: function (body) {
                console.log("会话创建成功！sessionId = " + body.sessionId);
                sessionLi.setAttribute('message-session-id', body.sessionId);
                console.log(sessionLi.getAttribute('message-session-id'));
                resolve(); // 表示 Promise 成功完成
            },
            error: function () {
                console.log("会话创建失败！");
                reject(); // 表示 Promise 失败
            }
        });
    });
}

function findSessionByName(username){
    //先获取所有会话列表的li标签
    //依次遍历，看看li标签里面谁的名字和要查找的名字一样
    let sessionLis = document.querySelectorAll('#session-list>li');
    for(let sessionLi of sessionLis){
        //获取到该li标签里面的h3
        let h3=sessionLi.querySelector('h3');
        if (h3.innerHTML === username){
            return sessionLi;
        }
    }
    return null;
}



////////////////////////////////
/////////添加、删除好友模块||搜索模块///////

function searchFriend(){
    let searchButton = document.getElementById('searchButton');
    let searchInput = document.getElementById('searchInput');
    let searchResults = document.querySelector('.hide-right ');
    let right = document.querySelector('.right');
    let right2 = document.querySelector('.hide-right2');
    let logo=document.querySelector('#rightLogo');

    // 点击搜索按钮时的处理函数
    searchButton.addEventListener('click', () => {

        // 获取输入框中的搜索关键词
        let keyword = searchInput.value.trim();
        // 检查搜索关键词是否为空
        if (keyword === "") {
            alert("请输入搜索关键词");
            return;
        }

        right.style.display = 'none';
        searchResults.style.display = 'block';
        right2.style.display = 'none';
        logo.style.display='none';
        let user_id;
        $.ajax({
            type: 'get',
            url: '/userInfo',
            success: function (body) {
                //从服务器获取到数据，校验结果是否有效，无效就跳转到登陆页面
                //有效就把用户名显示到界面上
                if (body.user_id && body.user_id > 0) {
                    user_id = body.user_id;
                } else {
                    alert("当前用户未登录!");
                    location.assign('/login.html');
                }
            },
            error: function () {
                console.log('搜索失败！')
            }
        });


        // 发送AJAX请求，将搜索关键词发送给后端
        // 后端提供了名为searchFriends的API接口来处理搜索请求
        let userDiv=document.querySelector('.main .left .user ');
        fetch('/searchUsers?username=' + searchInput.value +'&userId='+userDiv.getAttribute('user-id'))
            .then(response => response.json())
            .then(data => {
                // 处理返回的搜索结果并在页面上显示
                displaySearchResults(data,user_id);
            })
            .catch(error => {
                console.error('Error searching friends:', error);
            });
    });

    // 显示搜索结果的函数 搜索用户
    function displaySearchResults(results,userId) {
        // 清空之前的搜索结果

        let displayUser=document.getElementById('Users');
        displayUser.innerHTML='';
        // 将搜索结果显示在页面上
        results.forEach(result => {
            if (result.userId!==userId) {
                let resultDiv = document.createElement('li');
                resultDiv.classList.add('searchUserLi');
                let nameDiv=document.createElement('div');
                nameDiv.className='nameDiv';
                nameDiv.textContent = result.username;
                resultDiv.setAttribute('searchUser-id', result.userId);
                resultDiv.appendChild(nameDiv);
                let userAvatar = document.createElement('img');
                findUserAvatar(userAvatar, result.username);
                resultDiv.appendChild(userAvatar);
                if (result.status === false) {
                    resultDiv.onclick = function () {
                        let friend=new Friend();
                        friend.friendName = result.username;
                        friend.friendId = result.userId;
                        clickFriend(friend);
                    }
                    displayUser.appendChild(resultDiv);
                    return;
                }
                let input = document.createElement('input');
                input.type = 'text';
                input.className = 'userInput';
                input.placeholder = '备注';
                resultDiv.appendChild(input);
                let addDiv = document.createElement('div');
                addDiv.className = 'addDiv';
                addDiv.title = '添加好友';
                addDiv.onclick = function () {
                    addFriend(addDiv);
                }
                resultDiv.appendChild(addDiv);
                // 假设返回的搜索结果中包含用户名字段
                displayUser.appendChild(resultDiv);
            }

        });
    }

}

searchFriend();
function Friend() {
    this.friendName = "";
    this.friendId = 0;
}

function addFriend(addDiv){
    let resultDiv = addDiv.parentNode;
    if (resultDiv==null){
        return;
    }
    // 在 resultDiv 下查找子元素 input
    let input = resultDiv.querySelector('input.userInput');
    // 获取 input 的值
    console.log("Result Div:", resultDiv);
    let toUserId=resultDiv.getAttribute('searchUser-id');
    let req = {
        type: 'applyFriendship',
        toUserId: toUserId,
        content: input.value,

    };
    req=JSON.stringify(req);

    websocket.send(req);
    // e) 发送完成清空输入框
    input.value='';
}

//点击添加好友标签 获取申请者列表
function clickFriendApply(){
    //点击哪一个标签，此处对应的clickSession就能拿到哪个标签 logo眼
    let logo=document.querySelector('#rightLogo');
    let searchResults = document.querySelector('.hide-right');
    let right = document.querySelector('.right');
    let right2= document.querySelector('.hide-right2');
    logo.style.display='none';
    right.style.display = 'none';
    searchResults.style.display = 'none';
    right2.style.display = 'block';
    let userDiv=document.querySelector('.main .left .user ');
    let appliers = document.getElementById('Appliers');
    appliers.innerHTML='';
    $.ajax({
        type: "GET",
        url: "/applyList?userId=" + userDiv.getAttribute('user-id'),
        success: function(body) {
            body.forEach(function(user) {
                let applierDiv = document.createElement('li');
                applierDiv.className='applierLi';
                let userAvatar = document.createElement('img');
                findUserAvatar(userAvatar, user.username);
                applierDiv.appendChild(userAvatar);
                let nameDiv = document.createElement('div');
                nameDiv.className='applyNameDiv';
                nameDiv.textContent=user.username;
                applierDiv.appendChild(nameDiv);
                applierDiv.setAttribute('applier-id', user.userId);
                let applierContent = document.createElement('div');
                applierContent.className = 'applierContent';
                applierContent.innerHTML = user.content;
                applierDiv.appendChild(applierContent);
                //同意和拒绝按钮
                let agreeDiv = document.createElement('div');
                agreeDiv.className = 'agreeTab';
                agreeDiv.title='同意好友申请';
                agreeDiv.onclick = function (){
                    clickAgreeTab(agreeDiv);
                }
                applierDiv.appendChild(agreeDiv);
                let rejectDiv = document.createElement('div');
                rejectDiv.className = 'rejectTab';
                rejectDiv.title='拒绝好友申请';
                rejectDiv.onclick = function (){
                    clickRejectTab(rejectDiv);
                }
                applierDiv.appendChild(rejectDiv);
                // 假设返回的搜索结果中包含用户名字段
                appliers.appendChild(applierDiv);

            });
        },
        error: function(error) {
            // 处理错误情况
            console.error(error);
        }
    });
}

function clickAgreeTab(agreeDiv){
    let applierDiv = agreeDiv.parentElement;

    // 获取applierDiv中存储的用户ID（这里假设你在applierDiv上设置了applier-id属性来存储用户ID）
    let applierId = applierDiv.getAttribute('applier-id');
    let userDiv=document.querySelector('.main .left .user ');
    let userId=userDiv.getAttribute('user-id');
    // 使用$.ajax向后端发送接受好友请求的请求
    $.ajax({
        type: "PUT",
        url: "/acceptFriendship", // 这里根据你的后端URL进行调整
        data: {
            applierId: applierId,
            userId: userId
        },
        success: function(user) {
            // 后端返回成功的情况
            if (user.content==='Success') {
                console.log("接受好友请求成功！");
                clickApp(user,applierId);

            }else {
                console.log("接受好友请求失败！");

            }
        },
        error: function(error) {
            // 处理错误情况
            console.error("接受好友请求失败：", error);
        }
    });
}

//接受好友请求之后构建会话列表
async function clickApp(user,applierId){
    let userDiv=document.querySelector('.main .left .user ');
    let userId = userDiv.getAttribute('user-id');
    let sessionLi = findSessionByName(user.username);
    let sessionListUl = document.querySelector('#session-list');
    $.ajax({
        type: 'get',
        url: '/getSessionId',
        data: {
            userId: userId,
            friendId: applierId,
        },
        //TODO
        success: function (body) {
            if (body.sessionId>0) {
                if (sessionLi){
                    // 如果会话已存在，就把这个会话设置成选中状态，并且置顶
                    sessionListUl.insertBefore(sessionLi, sessionListUl.children[0]);
                    // 此处设置会话选中状态，获取历史消息，这两个功能在clickSession已经有了，直接调用clickSession即可
                }else {
                    let userAvatar = document.createElement('img');
                    findUserAvatar(userAvatar, user.username);
                    sessionLi = document.createElement('li');
                    sessionLi.innerHTML = '<h3>' + user.username + '</h3>' + '<p>' +
                        '我们已经是好友啦，快...</p>';
                    // 把标签进行置顶
                    sessionListUl.insertBefore(sessionLi, sessionListUl.children[0]);
                    sessionLi.onclick = function () {
                        clickSession(sessionLi);
                    }
                    sessionLi.setAttribute('message-session-id',body.sessionId);
                    sessionLi.appendChild(userAvatar);
                }
                sessionLi.click();
                let req = {
                    type: 'message',
                    sessionId: sessionLi.getAttribute('message-session-id'),
                    content: '我们已经是好友啦，快来和我发消息吧',

                };
                req = JSON.stringify(req);
                console.log("[websocket] send: " + req);
                // d) 通过websocket 发送消息
                websocket.send(req);
            } else {
                let userAvatar = document.createElement('img');
                findUserAvatar(userAvatar, user.username);
                sessionLi = document.createElement('li');
                sessionLi.innerHTML = '<h3>' + user.username + '</h3>' + '<p>' +
                    '我们已经是好友啦，快...</p>';
                // 把标签进行置顶
                sessionListUl.insertBefore(sessionLi, sessionListUl.children[0]);
                sessionLi.onclick = function () {
                    clickSession(sessionLi);
                }
                sessionLi.click();
                sessionLi.appendChild(userAvatar);
                // 发消息给服务器，新创建的会话啥样的，并等待createSession完成
                createApplySession(user.userId, sessionLi);
            }
            let userAvatar = document.createElement('img');
            findUserAvatar(userAvatar,user.username);
            sessionLi.appendChild(userAvatar);
            let searchResults = document.querySelector('.hide-right');
            let right = document.querySelector('.right');
            let right2 = document.querySelector('.hide-right2');
            let logo=document.querySelector('#rightLogo');
            logo.style.display='none';
            right.style.display = 'block';
            searchResults.style.display = 'none';
            right2.style.display = 'none';

            // 同时把标签页切换到会话列表
            let tabSession = document.querySelector('.tab .tab-session');
            tabSession.click();
        },
        error: function () {
            console.log("出现错误");
        }
    })

}
function createApplySession(friendId, sessionLi) {
    return new Promise(function(resolve, reject) {
        $.ajax({
            type: 'post',
            url: 'session?toUserId=' + friendId,
            success: function (body) {
                console.log("会话创建成功！sessionId = " + body.sessionId);
                sessionLi.setAttribute('message-session-id', body.sessionId);
                console.log(sessionLi.getAttribute('message-session-id'));
                let req = {
                    type: 'message',
                    sessionId: sessionLi.getAttribute('message-session-id'),
                    content: '我们已经是好友啦，快来和我发消息吧',

                };
                req = JSON.stringify(req);
                console.log("[websocket] send: " + req);
                // d) 通过websocket 发送消息
                websocket.send(req);
                resolve(); // 表示 Promise 成功完成
            },
            error: function () {
                console.log("会话创建失败！");
                reject(); // 表示 Promise 失败
            }
        });
    });
}

function clickRejectTab(rejectDiv){
    let applierDiv = rejectDiv.parentElement;
    //获取用户Id
    let applierId = applierDiv.getAttribute('applier-id');
    let userDiv=document.querySelector('.main .left .user ');
    let userId=userDiv.getAttribute('user-id');
    // 使用$.ajax向后端发送接受好友请求的请求
    $.ajax({
        type: "post",
        url: "/deleteFriendship", // 这里根据你的后端URL进行调整
        data: {
            userId: applierId,
            friendId: userId
        },
        success: function(response) {
            // 后端返回成功的情况
            if (response.content==='Success') {
                console.log("拒绝好友请求成功！");
                clickFriendApply();
            }else {
                console.log("拒绝好友请求失败！");

            }
        },
        error: function(error) {
            // 处理错误情况
            console.error("拒绝好友请求失败：", error);
        }
    });
}

const avatar = document.getElementById('avatarImage');
const modal = document.getElementById('avatarModal');

const showImgButton = document.getElementById('showImg');
const changeImgButton = document.getElementById('changeImg');
const closeAvatarModalButton = document.getElementById('closeAvatarModal');
const modalAvatar = document.getElementById('myModal');
const modalImg = document.getElementById('modalImg');
const closeBtn = document.querySelector("#myModal .close");
avatar.addEventListener('click', () => {
    modal.style.display = 'block';
    let img=avatar.src;
    console.log(img);
});
showImgButton.addEventListener('click', () => {
    // 响应展示头像按钮的操作

    let userDiv=document.querySelector(".left .user");
    let username=userDiv.textContent;
    $.ajax({
        type: "get",
        url: "/getAvatar",
        data:{
            username: username
        },
        success: function (body){
            if (body.avatarPath) {
                modalImg.src = body.avatarPath;
            }else {
                avatar.src='avatars/default-avatar.png';
            }
        },
        error: function (){
            console.log("出错了");
            avatar.src='avatars/default-avatar.png';
        }
    })
    modalAvatar.style.display = 'block';
    modal.style.display = 'none';
});

let lastChangeTime = 0;
const changeInterval = 5 * 60 * 1000; // 五分钟的毫秒数
let flag=false;
changeImgButton.addEventListener('click', () => {
    const currentTime = new Date().getTime();
    // 创建文件选择框元素
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    let userDiv=document.querySelector(".left .user");

    if (currentTime - lastChangeTime >= changeInterval||!flag) {
        flag=true;
        lastChangeTime = currentTime;
        fileInput.click();
        // 监听文件选择框的change事件
        fileInput.addEventListener('change', (event) => {
            const selectedFile = event.target.files[0];
            if (selectedFile) {
                // 获取文件扩展名
                const fileExtension = selectedFile.name.split('.').pop().toLowerCase();

                // 验证文件格式，这里以jpg和png为例
                // 在验证文件格式正确后执行提交操作
                if (fileExtension === 'jpg' || fileExtension === 'png') {
                    const formData = new FormData();
                    formData.append('avatar', selectedFile); // 'avatar' 是后端接收文件的字段名
                    formData.append('username', userDiv.textContent);
                    formData.append('userId', parseInt(userDiv.getAttribute("user-id")));
                    // 发起 AJAX 请求将文件上传到后端
                    fetch('/uploadAvatar', {
                        method: 'POST',
                        body: formData
                    })
                        .then(response => response.text())
                        .then(data => {
                            console.log('文件上传成功', data);
                            initAvatar();
                            modal.style.display = 'none';
                        })
                        .catch(error => {
                            console.error('文件上传失败', error);
                        });
                } else {
                    alert('请选择正确的图片格式（jpg或png）');
                }
                // 触发文件选择框点击事件


            }
        });
    }else {
        const remainingTime = Math.ceil((changeInterval - (currentTime - lastChangeTime)) / 1000);
        alert(`请在 ${remainingTime} 秒后再次尝试更换头像`);
        modal.style.display = 'none';
    }

});

closeBtn.addEventListener('click', () => {
    // 关闭弹出层
    modalAvatar.style.display = 'none';
});

closeAvatarModalButton.addEventListener('click', () => {
    modal.style.display = 'none';
});

modal.addEventListener('click', (event) => {
    if (event.target === modal) {
        modal.style.display = 'none';
    }
});


function initAvatar(){
    let userDiv=document.querySelector(".left .user");
    let username=userDiv.textContent;
    $.ajax({
        type: "get",
        url: "/getAvatar",
        data:{
            username: username
        },
        success: function (body){
            if (body.avatarPath) {
                avatar.src = body.avatarPath;
            }else {
                avatar.src='avatars/default-avatar.png';
            }
        },
        error: function (){
            console.log("出错了");

            avatar.src='avatars/default-avatar.png';
        }
    })
}

////////////////////////////////
//////////群聊功能/////////////


//点击好友后展示好友并让用户选择哪些加入群聊
function setGroupChat(){
    let friendList=document.querySelector('#groupChatPage .friendList ul');
    let groupChatPage=document.querySelector('.left .groupChatPage');
    groupChatPage.style.display='block';
    let userDiv=document.querySelector('.main .left .user ');
    let user_id=userDiv.getAttribute('user-id');
    let selectedFriendsList = document.querySelector('.selectedFriends ul');
    let cancel=document.querySelector('#groupChatPage .choice .cancel');
    let overlay = document.querySelector('.left .overlay');
    let overlay2 = document.querySelector('.left .overlay2');
    overlay2.style.display='block';
    fetch(`/friend/friends?user_id=${user_id}`)
        .then(response => response.json())
        .then(data => {
            friendList.innerHTML = '';
            renderFriendsList(data);
        })
        .catch(error => {
            console.assert('请求好友列表数据失败!');
            return;
        });

    function renderFriendsList(friendsData) {
        let friendTitle=document.querySelector('#right > div.title');
        friendsData.forEach(function(friend) {
            if (friendTitle.getAttribute('friendId')!=friend.friendId) {
                let listItem = document.createElement('li');
                let h4 = document.createElement('h4');
                h4.textContent = friend.friendName;
                listItem.appendChild(h4);
                let userAvatar = document.createElement('img');
                findUserAvatar(userAvatar, friend.friendName);
                listItem.appendChild(userAvatar);
                // 添加选择框
                let checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                listItem.appendChild(checkbox);

                listItem.classList.add('friend');
                listItem.setAttribute('friend-id', friend.friendId);
                friendList.appendChild(listItem);
            }
        });
    }

    // 监听选择框的变化
    friendList.addEventListener('change', function (event) {
        if (event.target.tagName === 'INPUT' && event.target.type === 'checkbox') {
            handleCheckboxChange(event.target);
        }
    });

    function handleCheckboxChange(checkbox) {
        let listItem = checkbox.closest('.friend');
        let friendId = listItem.getAttribute('friend-id');
        let friendName = listItem.querySelector('h4').textContent;

        if (checkbox.checked) {
            // 检查是否已添加过
            let existingItem = selectedFriendsList.querySelector(`li[friend-id="${friendId}"]`);
            if (!existingItem) {
                // 选中，将好友添加到已选择的好友列表
                let listItemClone = listItem.cloneNode(true); // 复制原始列表项
                listItemClone.removeChild(listItemClone.querySelector('input')); // 移除复选框
                addSelectedFriend(listItemClone);
            }
        } else {
            // 取消选中，将好友从已选择的好友列表中移除
            removeSelectedFriend(friendId);
        }
    }

    function addSelectedFriend(listItem) {
        selectedFriendsList.appendChild(listItem);
    }

    function removeSelectedFriend(id) {
        let listItem = selectedFriendsList.querySelector(`li[friend-id="${id}"]`);
        if (listItem) {
            selectedFriendsList.removeChild(listItem);
        }
    }

    cancel.addEventListener('click', function() {
        groupChatPage.style.display = 'none';
        clearSelectedFriendsList();
        overlay2.style.display='none';
    });

    function clearSelectedFriendsList() {
        let selectedFriendsList = document.querySelector('#groupChatPage .selectedFriends ul');
        selectedFriendsList.innerHTML = '';
    }


    let groupChatNameInput = document.querySelector('#groupChatName');
    let yesButton = document.querySelector('.inputGroupChatName .yes');
    let noButton = document.querySelector('.inputGroupChatName .no');
    let inputGroupChatName = document.querySelector('#inputGroupChatName');

    let submitButton = document.querySelector('#groupChatPage .choice .submit');
    submitButton.addEventListener('click', function () {
        let selectedFriendsList = document.querySelectorAll('.selectedFriends ul li[friend-id]');
        overlay.style.display = 'block';
        if (selectedFriendsList.length > 0) {
            // 显示群名输入框
            inputGroupChatName.style.display = 'block';
        } else {
            console.log('已选择的好友列表为空，无需提交。');
            overlay.style.display = 'none';
        }

    });


    yesButton.onclick=function () {
        let groupName = groupChatNameInput.value.trim();
        overlay.style.display = 'none'
        if (groupName) {
            let selectedFriendsList = document.querySelectorAll('.selectedFriends ul li[friend-id]');
            let friendIds = Array.from(selectedFriendsList).map(item => item.getAttribute('friend-id'));
            let title=document.querySelector('#right > div.title');
            let curFriendId=title.getAttribute('friendId');
            friendIds.push(curFriendId);

            if (groupName.length>10){
                alert('请输入10个字符以内的群名字');
                //清空已经选择的好友
                selectedFriendsList.innerHTML = '';
                // 隐藏输入框
                inputGroupChatName.style.display = 'none';
                overlay2.style.display='none';
                groupChatPage.style.display='none';
                // 清空输入框内容
                groupChatNameInput.value = '';
                return;
            }

            // 发送 JSON 数据到后端
            let requestBody = {
                friendIds: friendIds,
                groupName: groupName,
            };

            fetch('/createGroupChat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            })
                .then(response => response.json())
                .then(data =>{
                    // 处理响应，例如显示提示信息等

                    if (data.sessionId>0) {
                        let req = {
                            type: 'message',
                            sessionId: data.sessionId,
                            content: '我已经创建群聊，大家快来聊天吧',

                        };
                        req = JSON.stringify(req);
                        console.log("[websocket] send: " + req);
                        // 通过websocket 发送消息
                        websocket.send(req);
                    }
                })
                .catch(error => {
                    console.error('提交数据失败:', error);
                });

            // 清空输入框内容
            groupChatNameInput.value = '';

        }
        //清空已经选择的好友
        selectedFriendsList.innerHTML = '';
        // 隐藏输入框
        inputGroupChatName.style.display = 'none';
        overlay2.style.display='none';
        groupChatPage.style.display='none';
    };

    noButton.addEventListener('click', function () {
        // 清空输入框内容
        groupChatNameInput.value = '';

        // 隐藏输入框
        inputGroupChatName.style.display = 'none';
        overlay.style.display = 'none'
    });

}


function exitGroup(sessionId){
    $.ajax({
        type: 'post',
        url: '/exitGroup',
        data: {
            sessionId: sessionId
        },
        success: function (){
            console.log('推出群聊成功');
        },
        error: function (){
            console.log('推出群聊失败');

        }
    })
}
