package com.example.testsmpp

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.testsmpp", appContext.packageName)
    }


    val config = SmppSessionConfiguration().apply {
        host = "your-sms-gateway-host"
        port = 2775 // or whichever port your provider supports
        systemId = "your-system-id"
        password = "your-password"
        sourceAddressTon = TypeOfNumber.NATIONAL.value
        sourceAddressNpi = NumberingPlanIndicator.ISDN.value
    }

    val session = DefaultSmppSession(config, MySessionHandler())
    session.connectAndBind()


    // create a SubmitSm request
    val pdu = SubmitSm().apply {
        destinationAddress = "recipient-mobile-number"
        sourceAddress = "sender-mobile-number"
        shortMessage = "Hello, World!"
    }

    // send the request and wait for a response
    val response = session.sendRequestPdu(pdu, 5000L)

// handle the response
    if (response is SubmitSmResp && response.commandStatus == SmppConstants.STATUS_OK) {
        println("Message sent successfully")
    } else {
        println("Failed to send message, error: ${response.commandStatus}")
    }

    // receive an incoming message (blocking call)
    val pduResponse = session.nextPdu()
    if (pduResponse is DeliverSm) {
        println("Received message: ${pduResponse.shortMessage}")
    }
}