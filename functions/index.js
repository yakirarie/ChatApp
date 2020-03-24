const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

const admin = require('firebase-admin');
admin.initializeApp();

exports.newMessage = functions.database.ref('/latest-messages/{Id1}/{Id2}')
    .onWrite((change, context) => {

        const chatMessage = change.after.val();

        const receiverId = chatMessage.toId;
        const senderId = chatMessage.fromId;
        const messageId = chatMessage.id;
        const message = chatMessage.text;



        return admin.database().ref("/users/" + senderId).once('value').then(snap => {
            const senderUser = snap.val();

            return admin.database().ref("/users/" + receiverId).once('value').then(snap => {
                const token = snap.child("token").val();

                const payload = {
                    data: {
                        data_type: "direct_message",
                        title: "New Message From " + senderUser.username,
                        message: message,
                        message_id: messageId,

                        sender_id: senderUser.uid,
                        sender_username: senderUser.username,
                        sender_img: senderUser.profileImageUrl,
                        sender_token: senderUser.token
                    }
                };

                return admin.messaging().sendToDevice(token, payload)
                    .then(function (response) {
                        return console.log("Successfully sent message:", response);
                    })
                    .catch(function (error) {
                        return console.log("Error sending message:", error);
                    });
            });
        });
    });