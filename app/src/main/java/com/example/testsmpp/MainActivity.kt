package com.example.testsmpp
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.cloudhopper.commons.charset.CharsetUtil
import com.cloudhopper.smpp.SmppBindType
import com.cloudhopper.smpp.SmppSessionConfiguration
import com.cloudhopper.smpp.impl.DefaultSmppClient
import com.cloudhopper.smpp.pdu.SubmitSm
import com.cloudhopper.smpp.type.Address
import com.cloudhopper.smpp.type.SmppBindException
import com.cloudhopper.smpp.type.SmppChannelException
import com.cloudhopper.smpp.type.SmppInvalidArgumentException
import com.cloudhopper.smpp.type.SmppTimeoutException
import com.cloudhopper.smpp.type.UnrecoverablePduException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val hostEditText = findViewById<EditText>(R.id.editTextHost)
        val host = hostEditText.text.toString()

        val portEditText = findViewById<EditText>(R.id.editTextPort)
        val port = portEditText.text.toString()

        val usernameEditText = findViewById<EditText>(R.id.editTextUsername)
        val username = usernameEditText.text.toString()

        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val password = passwordEditText.text.toString()




        val submitButton = findViewById<Button>(R.id.buttonSubmit)
        submitButton.setOnClickListener {
            try {

                val client = DefaultSmppClient()

                val sessionConfig = SmppSessionConfiguration()
                sessionConfig.type = SmppBindType.TRANSCEIVER
                sessionConfig.host = host
                sessionConfig.port = port.toInt()
                sessionConfig.systemId = username
                sessionConfig.password = password

                val session = client.bind(sessionConfig)

                val sm = createSubmitSm("Test", "79111234567", "Hello", "UCS-2")

                println("Try to send message")

                session.submit(sm, TimeUnit.SECONDS.toMillis(60))

                println("Message sent")

                println("Wait 10 seconds")

                TimeUnit.SECONDS.sleep(10)

                println("Destroy session")

                session.close()
                session.destroy()

                println("Destroy client")

                client.destroy()

                println("Bye!")


            } catch (ex: SmppTimeoutException) {
                Log.v("session", "SmppTimeoutException")
            } catch (ex: SmppChannelException) {
                Log.v("session", "SmppChannelException")

            } catch (ex: SmppBindException) {
                Log.v("session",  "SmppBindException")

            } catch (ex: UnrecoverablePduException) {
                Log.v("session", "UnrecoverablePduException")

            } catch (ex: InterruptedException) {
                Log.v("session",  "InterruptedException")
            }


        }


    }

    @Throws(SmppInvalidArgumentException::class)
    fun createSubmitSm(src: String?, dst: String?, text: String?, charset: String?): SubmitSm? {
        val sm = SubmitSm()

        // For alpha numeric will use
        // TON=5
        // NPI=0
        sm.sourceAddress = Address(5.toByte(), 0.toByte(), src)

        // For national numbers will use
        // TON=1
        // NPI=1
        sm.destAddress = Address(1.toByte(), 1.toByte(), dst)

        // Set datacoding to UCS-2
        sm.dataCoding = 8.toByte()

        // Encode text
        sm.shortMessage = CharsetUtil.encode(text, charset)
        return sm
    }
}