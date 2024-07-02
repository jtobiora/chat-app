'use strict';

const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const usernameForm = document.querySelector('#usernameForm');
const messageForm = document.querySelector('#messageForm');
const groupMessageForm = document.querySelector('#groupMessageForm');
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
let selectedGroupName = null;

window.onload = function() {
    document.querySelector('.tablinks').click();
};

function openTab(evt, tabName) {
    const tabcontent = document.getElementsByClassName("tabcontent");
    for (let i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    const tablinks = document.getElementsByClassName("tablinks");
    for (let i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    document.getElementById(tabName).style.display = "block";
    evt.currentTarget.className += " active";
    if (tabName === 'GroupChat') {
        loadUserGroups();
    }

    // Show/hide message forms based on the selected tab
    if (tabName === 'PrivateChat') {
        document.getElementById('messageForm').classList.remove('hidden');
        document.getElementById('groupMessageForm').classList.add('hidden');
    } else if (tabName === 'GroupChat') {
        document.getElementById('messageForm').classList.add('hidden');
        document.getElementById('groupMessageForm').classList.remove('hidden');
    }
}

usernameForm.addEventListener('submit', connect, true); // step 1
messageForm.addEventListener('submit', sendMessage, true);
logout.addEventListener('click', onLogout, true);
window.onbeforeunload = () => onLogout();
createGroupForm.addEventListener('submit', createGroup, true);
addUserToGroupForm.addEventListener('submit', addUserToGroup, true);
groupMessageForm.addEventListener('submit', sendMessageToGroup, true);

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
    subscribeToUserGroups(); // Call this function to subscribe to user's groups
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

// Fetch and display user groups
async function loadUserGroups() {
    try {
        const userGroupsResponse = await fetch(`/users/groups/${nickname}`);
        if (!userGroupsResponse.ok) {
            throw new Error('Failed to fetch user groups');
        }
        const userGroups = await userGroupsResponse.json();
        const userGroupsList = document.getElementById('userGroups');
        userGroupsList.innerHTML = '';

        userGroups.forEach(group => {
            const listItem = document.createElement('li');
            listItem.textContent = group.name;
            userGroupsList.appendChild(listItem);
        });
    } catch (error) {
        console.error('Error loading user groups:', error);
    }
}

async function subscribeToUserGroups() {
    try {
        const userGroupsResponse = await fetch(`/users/groups/${nickname}`);
        if (!userGroupsResponse.ok) {
            throw new Error('Failed to fetch user groups');
        }
        const userGroups = await userGroupsResponse.json();
        userGroups.forEach(group => {
            stompClient.subscribe(`/topic/groups/${group.name}`, onGroupMessageReceived);
            console.log(`Subscribed to group: /topic/groups/${group.name}`);
        });
    } catch (error) {
        console.error('Error subscribing to user groups:', error);
    }
}

function selectGroup(event) {
    var selectedGroup = event.target.closest('li'); // Get the closest <li> element clicked
    if (!selectedGroup) return; // Do nothing if no <li> element is clicked

    // Clear active class from all list items
    var groups = document.getElementById('userGroups').getElementsByTagName('li');
    for (var i = 0; i < groups.length; i++) {
        groups[i].classList.remove('active');
    }

    // Add active class to the clicked group
    selectedGroup.classList.add('active');

    // Show/hide message form based on the selected tab
    const groupMessageForm = document.getElementById('groupMessageForm');
    if (selectedGroup) {
        groupMessageForm.classList.remove('hideme');
        selectedGroupName = selectedGroup.textContent;
    } else {
        groupMessageForm.classList.add('hideme');
        selectedGroupName = null;
    }

    // Logic to update chat area or perform other actions based on the selected group
    var groupId = selectedGroup.getAttribute('data-group-id');
}

function onGroupMessageReceived(payload) {
    const message = JSON.parse(payload.body);
    displayGroupMessage(message.senderId, message.content);
}

function sendMessageToGroup(event) {
    event.preventDefault();
    const groupMessageInput = document.getElementById('groupMessage').value.trim();

    if (groupMessageInput && stompClient) {
        const groupChatMessage = {
            senderId: nickname,
            content: groupMessageInput,
            groupName: selectedGroupName,
            timestamp: new Date()
        };

        console.log("Message to send: ", groupChatMessage);
        // Send message to the group via WebSocket
        stompClient.send("/app/chat.sendGroupMessage", {}, JSON.stringify(groupChatMessage));

        // Clear the input field
        document.getElementById('groupMessage').value = '';

        // Display the message in the chat area
        displayGroupMessage(nickname, groupChatMessage.content);
    }

    const chatArea = document.getElementById('chatArea');
    chatArea.scrollTop = chatArea.scrollHeight;
}

function displayGroupMessage(senderId, content) {
    const chatArea = document.getElementById('chatArea');
    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message');
    if (senderId === nickname) {
        messageContainer.classList.add('sender');
    } else {
        messageContainer.classList.add('receiver');
    }
    const message = document.createElement('p');
    message.textContent = `${senderId}: ${content}`;
    messageContainer.appendChild(message);
    chatArea.appendChild(messageContainer);
    chatArea.scrollTop = chatArea.scrollHeight;
}

