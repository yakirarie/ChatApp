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
            return console.log("Successfully sent message:", response);
        } catch (error) {
            return console.log("Error sending message:", error);
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

        // const recieversUsersPromises = []
        // for (const rec of receiversIds) {
        //     recieversUsersPromises.push(admin.database().ref("/users/" + rec).once('value'));
        // }
        // const recieversUsersSnapshots = await Promise.all(recieversUsersPromises);

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
                group_admin_uid: group.groupAdmin,
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
        // for (let i = 0; i < recieversUsersSnapshots.length; i++) {
        //     payload.data[`receiver_id${i+1}`] = recieversUsersSnapshots[i].val().uid;
        //     payload.data[`receiver_username${i+1}`] = recieversUsersSnapshots[i].val().username;
        //     payload.data[`receiver_img${i+1}`] = recieversUsersSnapshots[i].val().profileImageUrl;
        //     payload.data[`receiver_token${i+1}`] = recieversUsersSnapshots[i].val().token;

        // }
        // if (chatMessage.messageType === "image") {
        //     payload.notification.body = "";
        //     payload.notification.image = messageText;
        // }

        // if (chatMessage.messageType === "video") {
        //     payload.notification.body = "🎥 video file 🎥";

        // }
        const responses = []
        for (const user of group.usersList) {
            if (senderId !== user.uid){
                try {
                    const response = admin.messaging().sendToDevice(user.token, payload);
                    responses.push(response)
                } catch (error) {
                    console.log("Error sending message to "+user.username+":", error);
                }
            }
            
        }
        const responses_results = await Promise.all(responses);
        for (const res of responses_results)
            console.log("Successfully sent message", res);


    });