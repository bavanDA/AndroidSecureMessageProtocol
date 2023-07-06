package com.example.testsmpp

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.testsmpp.SecurityUtil.Companion.decryptText
import com.example.testsmpp.SecurityUtil.Companion.encryptText
import com.example.testsmpp.SecurityUtil.Companion.hashText
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    var sendVal = false
    var number = ""
    var text = ""

    var receiveVal = false
    var rNumber = ""
    var rText = ""

    var sendStatus = false
    private val permission: String = android.Manifest.permission.RECEIVE_SMS
    private val requestCode: Int = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        val numberEditText = findViewById<EditText>(R.id.number_input)
        val smppClient = Smpp(this)


        val sendSMSThread = Thread {
            try {
                while (true) {
                    if (sendVal) {
                        sendStatus = smppClient.sendSMS(number, text)
                        sendVal = false

                        runOnUiThread {
                            if (sendStatus)
                                showSendStatus(true,false)
                            else
                                showSendStatus(false,true)

                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        sendSMSThread.start()


        val receiveSMSThread = Thread {
            try {
                while (true) {
                    if (receiveVal) {
//                        smppClient.bindToSMSC()
                        Log.v("HASH BAVAN rText",rText)
                        val hText = hashText(rText)
                        val ackText = "$hText | AFTSTC"

                        smppClient.sendSMS(rNumber, ackText)
                        receiveVal = false




                    }
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        receiveSMSThread.start()

        val settingButton = findViewById<ImageButton>(R.id.settings_button)
        settingButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val smsButton = findViewById<ImageButton>(R.id.sms_button)
        smsButton.setOnClickListener {
            val intent = Intent(this, SMSActivity::class.java)
            startActivity(intent)
        }

        val sharedPref = this.getSharedPreferences("gateway_config", MODE_PRIVATE)
        val key = sharedPref.getString("key", "")
        val df: DateFormat = SimpleDateFormat("EEE, d MMM yyyy, HH:mm")


        val dateTextView = findViewById<TextView>(R.id.date_textview)
        var date: String = df.format(Calendar.getInstance().time)
        dateTextView.text = "Date: $date"

        val arriveButton = findViewById<Button>(R.id.arrive_button)
        arriveButton.setOnClickListener {
            showSendStatus(false,false)
            showAckStatus(false,false)
            number = numberEditText.text.toString()
            date= df.format(Calendar.getInstance().time)
            dateTextView.text = "Date: $date"
            text = "Arrival $date"
//            receiveVal = true
            sendVal = true;
        }


        val exitButton = findViewById<Button>(R.id.exit_button)
        exitButton.setOnClickListener {
            showSendStatus(false,false)
            showAckStatus(false,false)
            number = numberEditText.text.toString()
            date = df.format(Calendar.getInstance().time)
            text = "Exit $date"
            dateTextView.text = "Date: $date"
//            receiveVal = true
            sendVal = true;
        }

        val cryptButton = findViewById<Button>(R.id.crypt_button)
        cryptButton.setOnClickListener {
            val cryptTextView = findViewById<TextView>(R.id.crypt_text)
            cryptTextView.text = encryptText(text, key.toString())
        }

        val decryptButton = findViewById<Button>(R.id.decrypt_button)
        decryptButton.setOnClickListener {
            val decryptTextView = findViewById<TextView>(R.id.decrypt_text)
            decryptTextView.text = decryptText(text, key.toString())
        }

        val hashButton = findViewById<Button>(R.id.hash_button)
        hashButton.setOnClickListener {
            val hashTextView = findViewById<TextView>(R.id.hash_text)
            hashTextView.text = hashText(text)
        }


        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            startSMSListener()

        } else {
            startSMSListener()
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        receiveVal = intent.getBooleanExtra("isReceive", false) // Default value is 0 if not found
        rNumber = intent.getStringExtra("phoneNumber").toString()
        rText = intent.getStringExtra("text").toString()

        val aReceiveVal = intent.getBooleanExtra("ackReceive", false) // Default value is 0 if not found
        val aNumber = intent.getStringExtra("ackPhoneNumber").toString()
        val aText = intent.getStringExtra("ackText").toString()
        if (aReceiveVal) {
            val hashTmp = hashText(text)
            showSendStatus(true,false)
            Log.v("HASH BAVAN text ",text)
            Log.v("HASH BAVAN hashTmp ",  hashTmp)
            Log.v("HASH BAVAN aText ",aText)


            if(hashTmp==aText && aNumber == number)
                showAckStatus(true, false)
            else
                showAckStatus(false,true)
        }

    }

    private fun startSMSListener() {
        val smsReceiver = SMSReceiver()
        val intentFilter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        registerReceiver(smsReceiver, intentFilter)
    }

    private fun showAckStatus(sa: Boolean, fa: Boolean) {
        val saTextview = findViewById<TextView>(R.id.status_sa_textview)
        val faTextview = findViewById<TextView>(R.id.status_fa_textview)

        saTextview.visibility = if (sa) View.VISIBLE else View.INVISIBLE
        faTextview.visibility = if (fa) View.VISIBLE else View.INVISIBLE

    }

    private fun showSendStatus(sm: Boolean, fm: Boolean) {
        val smTextview = findViewById<TextView>(R.id.status_sm_textview)
        val fmTextview = findViewById<TextView>(R.id.status_fm_textview)

        smTextview.visibility = if (sm) View.VISIBLE else View.INVISIBLE
        fmTextview.visibility = if (fm) View.VISIBLE else View.INVISIBLE

    }
}