const functions = require('firebase-functions');
// Import and initialize the Firebase Admin SDK.
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
var topic='general_notifications';
//const path = require('path');
//const os = require('os');

///var uuid = require('uuid');
//const uuidv1 = require('uuid/v1');

const callbackHandler = require("./callbackUrl");

//calls the handler...
exports.mpesa = functions.https.onRequest(callbackHandler);

exports.notification = functions.database.ref('/announcements/{pushId}').onCreate((snapshot,context)=>{
    const notification = snapshot.val();
    const title = notification.title;
    const message = notification.message;

    notificationMessage = {
        data:{
          "title":title,
          "message":message
        },
        topic:topic
      };
    
    return admin.messaging().send(notificationMessage);
});


