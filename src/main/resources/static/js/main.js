'use strict';

const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const usernameForm = document.querySelector('#usernameForm');
const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const connectingElement = document.querySelector('.connecting');
const chatArea = document.querySelector('#chat-messages');
const logout = document.querySelector('#logout');
const createGroupForm = document.querySelector('#createGroupForm');
const addUserToGroupForm = document.querySelector('#addUserToGroupForm');

let stompClient = null;
let nickname = null;
let fullname = null;
let selectedUserId = null;


function connect(event) {
    nickname = document.querySelector('#nickname').value.trim();
    fullname = document.querySelector('#fullname').value.trim();

    if (nickname && fullname) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        const socket = new SockJS('https://localhost:8091/w_s');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}

function onConnected() {
    console.log(`Subscribing to::::: /queue/${nickname}/messages`);
    stompClient.subscribe(`/queue/${nickname}/messages`, onMessageReceived); //user subscribes to their own queue for receiving messages
    console.log(`Subscribed to: /queue/${nickname}/messages`);

    stompClient.subscribe(`/topic/public`, onMessageReceived);

    // register the connected user
    stompClient.send("/app/user.addUser",
        {},
        JSON.stringify({nickName: nickname, fullName: fullname, status: 'ONLINE'})
    );
    document.querySelector('#connected-user-fullname').textContent = fullname;
    findAndDisplayConnectedUsers().then();

    // Call findAndDisplayGroups after user is connected
    findAndDisplayGroups().then();
}

async function findAndDisplayConnectedUsers() {
    const connectedUsersResponse = await fetch('/users');
    let connectedUsers = await connectedUsersResponse.json();
    connectedUsers = connectedUsers.filter(user => user.nickName !== nickname);
    const connectedUsersList = document.getElementById('connectedUsers');
    connectedUsersList.innerHTML = '';

    connectedUsers.forEach(user => {
        appendUserElement(user, connectedUsersList);
        if (connectedUsers.indexOf(user) < connectedUsers.length - 1) {
            const separator = document.createElement('li');
            separator.classList.add('separator');
            connectedUsersList.appendChild(separator);
        }
    });
}

function appendUserElement(user, connectedUsersList) {
    const listItem = document.createElement('li');
    listItem.classList.add('user-item');
    listItem.id = user.nickName;

    const userImage = document.createElement('img');
    userImage.src = '../img/user_icon.png';
    userImage.alt = user.fullName;

    const usernameSpan = document.createElement('span');
    usernameSpan.textContent = user.fullName;

    const receivedMsgs = document.createElement('span');
    receivedMsgs.textContent = '0';
    receivedMsgs.classList.add('nbr-msg', 'hidden');

    listItem.appendChild(userImage);
    listItem.appendChild(usernameSpan);
    listItem.appendChild(receivedMsgs);

    listItem.addEventListener('click', userItemClick);

    connectedUsersList.appendChild(listItem);
}

function userItemClick(event) {
    document.querySelectorAll('.user-item').forEach(item => {
        item.classList.remove('active');
    });
    messageForm.classList.remove('hidden');

    const clickedUser = event.currentTarget;
    clickedUser.classList.add('active');

    selectedUserId = clickedUser.getAttribute('id');
    fetchAndDisplayUserChat().then();

    const nbrMsg = clickedUser.querySelector('.nbr-msg');
    nbrMsg.classList.add('hidden');
    nbrMsg.textContent = '0';
}

function displayMessage(senderId, content) {
    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message');
    if (senderId === nickname) {
        messageContainer.classList.add('sender');
    } else {
        messageContainer.classList.add('receiver');
    }
    const message = document.createElement('p');
    message.textContent = content;
    messageContainer.appendChild(message);
    chatArea.appendChild(messageContainer);
}

async function fetchAndDisplayUserChat() {
    console.log("Fetching chat ......");
    const userChatResponse = await fetch(`/messages/${nickname}/${selectedUserId}`);
    const userChat = await userChatResponse.json();
    chatArea.innerHTML = '';
    userChat.forEach(chat => {
        displayMessage(chat.senderId, chat.content);
    });
    chatArea.scrollTop = chatArea.scrollHeight;
}

function onError() {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

function sendMessage(event) {
    const messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        const chatMessage = {
            senderId: nickname,
            recipientId: selectedUserId,
            content: messageInput.value.trim(),
            timestamp: new Date()
        };
        stompClient.send("/app/chat", {}, JSON.stringify(chatMessage));
        displayMessage(nickname, messageInput.value.trim());
        messageInput.value = '';
    }
    chatArea.scrollTop = chatArea.scrollHeight;
    event.preventDefault();
}

async function onMessageReceived(payload) {
    console.log("message received >>>>>>> ", payload);
    await findAndDisplayConnectedUsers();
    const message = JSON.parse(payload.body);
    if (selectedUserId && selectedUserId === message.senderId) {
        displayMessage(message.senderId, message.content);
        chatArea.scrollTop = chatArea.scrollHeight;
    }

    if (selectedUserId) {
        document.querySelector(`#${selectedUserId}`).classList.add('active');
    } else {
        messageForm.classList.add('hidden');
    }

    const notifiedUser = document.querySelector(`#${message.senderId}`);
    if (notifiedUser && !notifiedUser.classList.contains('active')) {
        const nbrMsg = notifiedUser.querySelector('.nbr-msg');
        nbrMsg.classList.remove('hidden');
        nbrMsg.textContent = '';
    }
}

function onLogout() {
    stompClient.send("/app/user.disconnectUser",
        {},
        JSON.stringify({nickName: nickname, fullName: fullname, status: 'OFFLINE'})
    );
    window.location.reload();
}

usernameForm.addEventListener('submit', connect, true); // step 1
messageForm.addEventListener('submit', sendMessage, true);
logout.addEventListener('click', onLogout, true);
window.onbeforeunload = () => onLogout();

createGroupForm.addEventListener('submit', createGroup, true);
addUserToGroupForm.addEventListener('submit', addUserToGroup, true);

async function createGroup(event) {
    event.preventDefault();
    const groupName = document.querySelector('#groupName').value.trim();
    if (groupName) {
        const response = await fetch('/user-groups', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ name: groupName })
        });

        if (response.ok) {
            document.querySelector('#groupName').value = '';
            await findAndDisplayGroups();
        } else {
            console.error('Failed to create group');
        }
    }
}

// async function addUserToGroup(event) {
//     event.preventDefault();
//     const groupId = document.querySelector('#groupId').value;
//     const userId = document.querySelector('#userId').value;
//     if (groupId && userId && stompClient) {
//         stompClient.send("/app/group.addUser", {},
//             JSON.stringify({ groupId: groupId, userId: userId }));
//         await findAndDisplayGroups();
//     }
// }

async function addUserToGroup(event) {
    event.preventDefault();
    const groupId = document.querySelector('#groupId').value;
    const userId = document.querySelector('#userId').value;
    if (groupId && userId) {
        try {
            const response = await fetch(`/users/${userId}/groups/${groupId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({})
            });
            if (response.ok) {
                // Call findAndDisplayGroups after adding user to group
                await findAndDisplayGroups();
            } else {
                console.error('Failed to add user to group');
            }
        } catch (error) {
            console.error('Error adding user to group:', error);
        }
    }
}

async function findAndDisplayGroups() {
    const groupsResponse = await fetch('/user-groups');
    const groups = await groupsResponse.json();
    const groupList = document.getElementById('userGroups');
    groupList.innerHTML = '';

    groups.forEach(group => {
        const listItem = document.createElement('li');
        listItem.classList.add('group-item');
        listItem.textContent = group.name;
        groupList.appendChild(listItem);
    });

    const groupSelect = document.querySelector('#groupId');
    groupSelect.innerHTML = '';
    groups.forEach(group => {
        const option = document.createElement('option');
        option.value = group.id;
        option.textContent = group.name;
        groupSelect.appendChild(option);
    });

    const usersResponse = await fetch('/users');
    const users = await usersResponse.json();
    const userSelect = document.querySelector('#userId');
    userSelect.innerHTML = '';
    users.forEach(user => {
        //if (user.nickName !== nickname) {
            const option = document.createElement('option');
            option.value = user.nickName;
            option.textContent = user.fullName;
            userSelect.appendChild(option);
       // }
    });
}

