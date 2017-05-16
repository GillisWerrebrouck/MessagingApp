const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref("notifications/{uid}")
.onWrite(event => {
	var request = event.data.val();
	
	var payload = {
		data: {
			uid: request.uid,
			sender: request.sender,
			message: request.message,
			messageKey: request.messageKey
		}
	};
	
	admin.messaging().sendToTopic(request.topic, payload)
	.then(function(response){
		console.log("Successfully sent message", response);
	})
	.catch(function(error){
		console.log("Error sending message", error);
	})
})