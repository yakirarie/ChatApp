const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

const admin = require('firebase-admin');
admin.initializeApp();


// listen to 2 users chat
exports.newMessageTwoUsers = functions.database.ref('/latest-messages/{id1}/{id2}')
    .onWrite(async (change, context) => {

        const chatMessage = change.after.val();
        console.log(chatMessage);
        const receiverId = chatMessage.toId[0];
        const senderId = chatMessage.fromId;
        const messageText = chatMessage.text;

        if (context.params.id1 !== senderId)
            return

        const snap = await admin.database().ref("/users/" + senderId).once('value');
        const senderUser = snap.val();
        const snap_1 = await admin.database().ref("/users/" + receiverId).once('value');
        const recieverUser = snap_1.val();
        const payload = {

            notification: {
                title: "New Message From " + senderUser.username,
                body: messageText
            },
            data: {

                receiver_id: recieverUser.uid,
                receiver_username: recieverUser.username,
                receiver_img: recieverUser.profileImageUrl,
                receiver_token: recieverUser.token,

                sender_id: senderUser.uid,
                sender_username: senderUser.username,
                sender_img: senderUser.profileImageUrl,
                sender_token: senderUser.token
            }

        };

        if (chatMessage.messageType === "image") {
            payload.notification.body = "";
            payload.notification.image = messageText;
        }

        if (chatMessage.messageType === "video") {
            payload.notification.body = "🎥 video file 🎥";

        }
        try {
            const response = await admin.messaging().sendToDevice(recieverUser.token, payload);
            console.log("Successfully sent message:", response);
        } catch (error) {
            console.log("Error sending message:", error);
        }
    });

// listen to group chat
exports.newMessageGroup = functions.database.ref('/group-messages/{groupId}/{messageId}')
    .onWrite(async (change, context) => {

        const chatMessage = change.after.val();

        const receiversIds = []

        for (let i = 0; i < chatMessage.toId.length; i++)
            receiversIds.push(chatMessage.toId[i]);
        const senderId = chatMessage.fromId;
        const messageText = chatMessage.text;


        const snap = await admin.database().ref("/users/" + senderId).once('value');
        const senderUser = snap.val();

        const snapGroup = await admin.database().ref("/users/" + context.params.groupId).once('value');
        const group = snapGroup.val();

        const payload = {

            notification: {
                title: "New Message From " + senderUser.username + " In " + group.groupName,
                body: messageText
            },
            data: {
                sender_id: senderUser.uid,
                sender_username: senderUser.username,
                sender_img: senderUser.profileImageUrl,
                sender_token: senderUser.token,

                group_id: group.uid,
                group_admin_uid: group.groupAdminUID,
                group_name: group.groupName,
                group_image: group.groupImageUrl,
                group_size: group.usersList.length.toString()


            }

        };
        for (let i = 0; i < group.usersList.length; i++) {
            payload.data[`receiver_id${i+1}`] = group.usersList[i].uid;
            payload.data[`receiver_username${i+1}`] = group.usersList[i].username;
            payload.data[`receiver_img${i+1}`] = group.usersList[i].profileImageUrl;
            payload.data[`receiver_token${i+1}`] = group.usersList[i].token;
        }

        // if (chatMessage.messageType === "image") {
        //     payload.notification.body = "";
        //     payload.notification.image = messageText;
        // }

        // if (chatMessage.messageType === "video") {
        //     payload.notification.body = "🎥 video file 🎥";

        // }

        const responses = []
        for (const user of group.usersList) {
            if (senderId !== user.uid) {
                try {
                    const response = admin.messaging().sendToDevice(user.token, payload);
                    responses.push(response)
                } catch (error) {
                    console.log("Error sending message to " + user.username + ":", error);
                }
            }

        }
        const responses_results = await Promise.all(responses);
        for (const res of responses_results)
            console.log("Successfully sent message", res);


    });

// listen for new group creation
exports.newGroupCreation = functions.database.ref('/users/{groupId}')
    .onWrite(async (change, context) => {

        const newGroup = change.after.val();
        if (newGroup.groupName === null) return
        const adminUser = newGroup.usersList.filter(user => user.uid === newGroup.groupAdminUID)[0];

        const payload = {

            notification: {
                title: adminUser.username + " Created " + newGroup.groupName,
                body: "You, " + adminUser.username + " and " + newGroup.usersList.length.toString() + " more"
            },
            data: {
                sender_id: adminUser.uid,
                sender_username: adminUser.username,
                sender_img: adminUser.profileImageUrl,
                sender_token: adminUser.token,

                group_id: newGroup.uid,
                group_admin_uid: newGroup.groupAdminUID,
                group_name: newGroup.groupName,
                group_image: newGroup.groupImageUrl,
                group_size: newGroup.usersList.length.toString()

            }

        };

        for (let i = 0; i < newGroup.usersList.length; i++) {
            payload.data[`receiver_id${i+1}`] = newGroup.usersList[i].uid;
            payload.data[`receiver_username${i+1}`] = newGroup.usersList[i].username;
            payload.data[`receiver_img${i+1}`] = newGroup.usersList[i].profileImageUrl;
            payload.data[`receiver_token${i+1}`] = newGroup.usersList[i].token;
        }


        const responses = []
        for (const user of newGroup.usersList) {
            if (newGroup.groupAdminUID !== user.uid) {
                try {
                    const response = admin.messaging().sendToDevice(user.token, payload);
                    responses.push(response)
                } catch (error) {
                    console.log("Error sending message to " + user.username + ":", error);
                }
            }

        }
        const responses_results = await Promise.all(responses);
        for (const res of responses_results)
            console.log("Successfully sent message", res);


    });