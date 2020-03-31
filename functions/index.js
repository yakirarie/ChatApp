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
exports.newMessage = functions.database.ref('/latest-messages/{id1}/{id2}')
    .onWrite(async (change, context) => {

        const chatMessage = change.after.val();

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
exports.newMessage = functions.database.ref('/group-messages/{id}')
    .onWrite(async (change, context) => {

        const chatMessage = change.after.val();

        const receiversIds = chatMessage.toId;
        const senderId = chatMessage.fromId;
        const messageText = chatMessage.text;


        const snap = await admin.database().ref("/users/" + senderId).once('value');
        const senderUser = snap.val();
        const recieversUsers = []
        for (const reciever of receiversIds){
            const snap_1 = admin.database().ref("/users/" + reciever).once('value');
            recieversUsers.push(snap_1.val());
        }
        await Promise.all(recieversUsers);
       
        const payload = {

            notification: {
                title: "New Message From " + senderUser.username,
                body: messageText
            },
            data: {
                sender_id: senderUser.uid,
                sender_username: senderUser.username,
                sender_img: senderUser.profileImageUrl,
                sender_token: senderUser.token
            }

        };
        for (let i = 0; i < receiversIds.length ; i++){
            payload.data[`receiver_id${i+1}`] = receiversIds[i].uid;
            payload.data[`receiver_username${i+1}`] = receiversIds[i].username;
            payload.data[`receiver_img${i+1}`] = receiversIds[i].profileImageUrl;
            payload.data[`receiver_token${i+1}`] = receiversIds[i].token;

        }
        console.log(payload);

        // if (chatMessage.messageType === "image") {
        //     payload.notification.body = "";
        //     payload.notification.image = messageText;
        // }

        // if (chatMessage.messageType === "video") {
        //     payload.notification.body = "🎥 video file 🎥";

        // }
        const responses = []
        for (const reciever of receiversIds){
            try {
                const response = admin.messaging().sendToDevice(reciever.token, payload);
                responses.push(response)
                return console.log("Successfully sent message:", response);
            } catch (error) {
                return console.log("Error sending message:", error);
            }
        }
        await Promise.all(responses);
       
    });