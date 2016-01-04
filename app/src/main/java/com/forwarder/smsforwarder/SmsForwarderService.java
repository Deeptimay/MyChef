package com.forwarder.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Deeptimay on 1/4/2016.
 */
public class SmsForwarderService extends BroadcastReceiver {

    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;
    Context context;

    // Get the object of SmsManager
    final SmsManager sms = SmsManager.getDefault();

    public void onReceive(Context context, Intent intent) {

        this.context = context;
        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (String key : bundle.keySet()) {
                    Log.d("SmsForwarder", key);
                }
                if (pdusObj != null) {
                    for (Object aPdusObj : pdusObj) {

                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);

                        Log.d("SmsForwarder", String.valueOf(currentMessage));

                        String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                        String senderNum = phoneNumber;
                        String message = currentMessage.getDisplayMessageBody();

                        Log.i("SmsForwarder", "senderNum: " + senderNum + "; message: " + message);

                        // Show Alert

                        Toast.makeText(context,
                                "senderNum: " + senderNum + ", message: " + message, Toast.LENGTH_SHORT).show();

//                        postNewComment(context, message, senderNum);

                        DatabaseHandler db = new DatabaseHandler(context);

                        /**
                         * CRUD Operations
                         * */
                        // Inserting Contacts
                        Log.d("Insert: ", "Inserting ..");
                        db.addContact(new SMSDataType(message, senderNum));

                    }
                }// end for loop
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);

        }
        synCallData(context);
    }

    public static void synCallData(Context context) {
        DatabaseHandler db = new DatabaseHandler(context);
        List<SMSDataType> allSms = db.getAllContacts();
        for (int i = 0; i < allSms.size(); i++) {
            postNewComment(context, allSms.get(i));
        }
    }

    public static void postNewComment(Context context, final SMSDataType sms) {
//        mPostCommentResponse.requestStarted();
        RequestQueue queue = Volley.newRequestQueue(context);
        final DatabaseHandler db = new DatabaseHandler(context);
        StringRequest sr = new StringRequest(Request.Method.POST, "http://apis.voicetree.co/insms/index.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                mPostCommentResponse.requestCompleted();
                Log.d("response", response);
                db.deleteContact(sms);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                mPostCommentResponse.requestEndedWithError(error);
                Log.d("response", String.valueOf(error));

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Body", sms._body);
                params.put("Number", sms._phone_number);

                Log.d("Body", sms._body);
                Log.d("Number", sms._phone_number);

                return params;
            }

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String,String> params = new HashMap<String, String>();
//                params.put("Content-Type","application/x-www-form-urlencoded");
//                return params;
//            }
        };
        queue.add(sr);
    }
}