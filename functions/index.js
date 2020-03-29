const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

const admin = require('firebase-admin');
admin.initializeApp();

exports.newMessage = functions.database.ref('/latest-messages/{id1}/{id2}')
    .onWrite(async (change, context) => {

        const chatMessage = change.after.val();

        const receiverId = chatMessage.toId;
        const senderId = chatMessage.fromId;
        const messageText = chatMessage.text;
        console.log("sender params", context.params.id1);
        console.log("sender chat", senderId);



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

        if (chatMessage.image) {
            payload.notification.body = "";
            payload.notification.image = messageText;
        }
        try {
            const response = await admin.messaging().sendToDevice(recieverUser.token, payload);
            return console.log("Successfully sent message:", response);
        } catch (error) {
            return console.log("Error sending message:", error);
        }
    });