const admin = require("firebase-admin");

const app = require("express")();
const cors = require("cors");
const parse = require("./parse");

const db = admin.database();
const store = admin.firestore();


app.use(cors({ origin: true }));

app.post("/", (request, response) => {

  var emailAddress = request.query.email;
  var token = request.query.token;
  var transactionType = parseInt(request.query.type);
  var semester = request.query.semester;
  
  const callbackData = request.body.Body.stkCallback;
  const parsedData = parse(callbackData);

  var data = null;

  var notificationMessage = {
    data:{
      "title":"Bitsa",
      "message":"Transaction not successful"
    }
  };

  if(parsedData.resultCode === 0){

    data = {
      amount:parsedData.amount,
      merchantRequestID:parsedData.merchantRequestID,
      referenceNumber:parsedData.mpesaReceiptNumber,
      email:emailAddress,
      semester:semester,
      transactionMethod:1,
      phoneNumber:parsedData.phoneNumber,
      type:transactionType,
      date:parsedData.transactionDate
     }

     notificationMessage = {
       data:{
         "title":"Bitsa",
         "message":"Successfully received your contribution. God bless"
       }
     };
     
   }

   let message = {
       	  "ResponseCode": "00000000",
       	  "ResponseDesc": "success"
   };
   
   if(data!==null){
       return admin.database().ref('transactions/').push(data).
    then(()=>{
       console.log("Transaction ("+parsedData.mpesaReceiptNumber+") recorded for user "+emailAddress);
       if(token!==null){
         console.log("token is not null -"+token)
         return admin.messaging().sendToDevice(token,notificationMessage).then(()=>{
           return response.json(message);
         });
       }else{
        return response.json(message);
       }
    })
  }else{
    if(token!==null){
      return admin.messaging().sendToDevice(token,notificationMessage).then(()=>{
        return response.json(message);
      });
    }else{
     response.json(message);
    }
  }

});

module.exports = app;
