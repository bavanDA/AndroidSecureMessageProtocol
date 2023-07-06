package com.example.testsmpp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testsmpp.SecurityUtil.Companion.decryptText
import com.example.testsmpp.SecurityUtil.Companion.encryptText
import com.example.testsmpp.SecurityUtil.Companion.hashText


class SMSActivity : AppCompatActivity() {

    private val permission: String = Manifest.permission.READ_SMS
    private val requestCode: Int = 1
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatItems: MutableList<ChatAdapter.ChatItem>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)
        supportActionBar?.hide()


        chatItems = if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            readSms() as MutableList<ChatAdapter.ChatItem>

        } else {
            readSms() as MutableList<ChatAdapter.ChatItem>
        }


//         Initialize the RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        // Create and set up the ChatAdapter
        chatAdapter = ChatAdapter(chatItems)
        recyclerView.adapter = chatAdapter

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

    }


    // Add a method to update the chat items in the adapter
    private fun updateChatItems(newChatItems: List<ChatAdapter.ChatItem>) {
        chatItems.clear()
        chatItems.addAll(newChatItems)
        chatAdapter.notifyDataSetChanged()
    }

    // Example method to update the chat items (you can call this whenever you have new chat items)
    private fun loadChatItems() {
        // Retrieve or generate your List<ChatItem>
        val newChatItems = readSms()
        updateChatItems(newChatItems)
    }

    private fun readSms(): List<ChatAdapter.ChatItem> {

        val sharedPref = this.getSharedPreferences("gateway_config", MODE_PRIVATE)
        val key = sharedPref.getString("key", "")

        val numberCol = Telephony.TextBasedSmsColumns.ADDRESS
        val textCol = Telephony.TextBasedSmsColumns.BODY
        val typeCol = Telephony.TextBasedSmsColumns.TYPE // 1 - Inbox, 2 - Sent

        val projection = arrayOf(numberCol, textCol, typeCol)

        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection, null, null, null
        )

        val numberColIdx = cursor!!.getColumnIndex(numberCol)
        val textColIdx = cursor.getColumnIndex(textCol)
        val typeColIdx = cursor.getColumnIndex(typeCol)

        val chatItems = mutableListOf<ChatAdapter.ChatItem>()

        while (cursor.moveToNext()) {
            val number = cursor.getString(numberColIdx)
            var text = cursor.getString(textColIdx)
            val type = cursor.getString(typeColIdx)

            if (text.contains("SMPP")) {
                text = text.replace(
                    "\n sent by Ozeki SMPP SMS Gateway for Android - www.ozekisms.com",
                    ""
                )
                text = text.replace(
                    "sent by Ozeki SMPP SMS Gateway for Android - www.ozekisms.com",
                    ""
                )
                Log.d("MY_APP", "$number $text $type")
                try {
                    val deText = decryptText(cursor.getString(textColIdx), key.toString())
                    if(type=="2")
                        chatItems.add(ChatAdapter.ChatItem(number, deText))
                } catch (e: Exception) {
                    chatItems.add(ChatAdapter.ChatItem(number, text))
                    Log.v("decrypt","Error occurred during decryption: ${e.message}")
                }

            }
        }

        cursor.close()

        return chatItems

    }
}