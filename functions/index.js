const functions = require('firebase-functions');
const admin = require('firebase-admin'); 

admin.initializeApp(); 

exports.sendNotificationToUser = functions.https.onCall((data, context) => {
    const title = data.title;
    const message = data.message;
    const token = data.token;

    if (!title || !message || !token) {
        throw new functions.https.HttpsError('invalid-argument', 'Invalid arguments provided');
    }

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
            console.log("Response from sendToDevice:", response); 
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
        })
        .then(() => {
            return { success: true };
        })
        .catch((error) => {
            console.error('Error sending notification to', token, error);
            if (error.code === 'messaging/invalid-registration-token' ||
                error.code === 'messaging/registration-token-not-registered') {
                // Remove the invalid/old token from the Firestore collection
                return admin.firestore().collection('users').doc(token).delete()
                    .then(() => {
                        console.log('Invalid/old token removed from Firestore:', token);
                        return { success: false, error: error.message };
                    })
                    .catch((err) => {
                        console.error('Error removing invalid/old token from Firestore:', err);
                        return { success: false, error: error.message };
                    });
            } else {
                return { success: false, error: error.message };
            }
        });
});