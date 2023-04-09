const functions = require('firebase-functions');
const admin = require('firebase-admin'); // Import the Firebase Admin SDK

admin.initializeApp(); // Initialize the Firebase Admin SDK

exports.sendNotificationToUser = functions.https.onCall((data, context) => {
    const title = data.title;
    const message = data.message;
    const token = data.token;
    const payload = {
        data: {
            title: title,
            message: message
        },
        notification: {
            title: title,
            body: message,
            sound: "default"
        }
    };
    const options = {
        priority: "high",
        timeToLive: 60 * 60 * 24 // 1 day
    };
    return admin.messaging().sendToDevice(token, payload, options)
        .then((response) => {
            console.log("Response from sendToDevice:", response); // add this line
            const tokensToRemove = [];
            response.results.forEach((result, index) => {
                const error = result.error;
                if (error) {
                    console.error('Failure sending notification to', token, error);
                    if (error.code === 'messaging/invalid-registration-token' ||
                        error.code === 'messaging/registration-token-not-registered') {
                        tokensToRemove.push(token);
                    }
                }
            });
            return Promise.all(tokensToRemove.map(tokenToRemove => admin.firestore().collection('users').doc(tokenToRemove).delete()));
        });
    return admin.messaging().sendToDevice(token, payload, options)
    .then((response) => {
        console.log("Response from sendToDevice:", response); // add this line
        const tokensToRemove = [];
        // rest of the code
    }); 
});



// exports.sendNotificationToUser = functions.https.onCall((data, context) => {
//     const title = data.title;
//     const message = data.message;
//     const token = data.token;

//     const payload = {
//         notification: {
//             title: title,
//             body: message
//         }
//     };

//     const options = {
//         priority: "high",
//         timeToLive: 60 * 60 * 24 // 1 day
//     };

//     return admin.messaging().sendToDevice(token, payload, options);
// });



// const admin = require('firebase-admin');
// admin.initializeApp();

// exports.sendNotificationToUser = functions.https.onCall(async (data, context) => {
//   const message = {
//     notification: {
//       title: data.title,
//       body: data.message,
//       clickAction: 'FLUTTER_NOTIFICATION_CLICK',
//     },
//     token: data.token,
//   };
//   try {
//     await admin.messaging().send(message);
//     return 'Notification sent successfully.';
//   } catch (error) {
//     console.error('Failed to send notification:', error);
//     throw new functions.https.HttpsError('internal', 'Failed to send notification.');
//   }
// });