package com.example.testsmpp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in smsMessages) {
                val body = message.messageBody
                val phoneNumber =  message.displayOriginatingAddress
                Log.v("SMSReceiver",body)
                Log.v("SMSReceiver",phoneNumber.removePrefix("+98"))

//
                val sharedPref = context.getSharedPreferences("gateway_config",
                    AppCompatActivity.MODE_PRIVATE
                )
                val key = sharedPref.getString("key", "")

                try {
                    val deText = SecurityUtil.decryptText(body, key.toString())
                    val mainActivityIntent = Intent(context, MainActivity::class.java)
                    if(!deText.contains("| AFTSTC")) {
                         //Create a new intent to send the data to MainActivity
                        mainActivityIntent.putExtra("isReceive", true)
                        mainActivityIntent.putExtra("phoneNumber", phoneNumber.removePrefix("+98"))
                        mainActivityIntent.putExtra("text", deText)


                    }
                    else
                    {
                        mainActivityIntent.putExtra("ackReceive", true)
                        mainActivityIntent.putExtra("ackPhoneNumber", phoneNumber.removePrefix("+98"))
                        mainActivityIntent.putExtra("ackText", deText.removeSuffix(" | AFTSTC"))
                    }

                    // Start the MainActivity with the new intent
                    mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    context.startActivity(mainActivityIntent)


                } catch (e: Exception) {
                    Log.v("decrypt","Error occurred during decryption: ${e.message}")
                }


            }
        }
    }
}