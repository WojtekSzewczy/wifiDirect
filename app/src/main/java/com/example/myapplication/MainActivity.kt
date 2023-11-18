package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var receiver: WifiBroadcastReceiver
    private val INTENT_IDENTIFIER = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = WifiBroadcastReceiver(this)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    setUIState()
                }
            }
        }
    }

    @Composable
    private fun setUIState() {

        if (connectionState()) { // sprawdza czy jesteśmy połączeni z innym urządzeniem
            CreateConnectedUI()
        } else {
            CreateScanerUI()
        }
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        Log.v("MainActivity", "openFile")
        startActivityForResult(intent, INTENT_IDENTIFIER)
    }


    @Composable
    private fun CreateScanerUI() {

        val context = LocalContext.current
        val value = getDevices()
        Log.v("MainActivity", value.toString())
        if (value != null) {
            val size = value.deviceList!!.toList().size
            val list = value.deviceList?.toList()
            Column {
                Button(onClick = { }) {} //TODO start scan po bożemu
                LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
                    items(size) { number ->
                        GreetingView(name = list?.get(number)?.deviceAddress + list?.get(number)?.deviceName) {
                            list?.get(number)?.let { receiver.connect(it) }
                            Toast.makeText(
                                context,
                                list?.get(number)?.deviceAddress,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }

        }

    }

    @Composable
    private fun CreateConnectedUI() {
        var messageToSend by remember { mutableStateOf(TextFieldValue("")) }
        var receivedMessage by remember { mutableStateOf("message not received") }

        Column {
            TextField(
                value = messageToSend,
                onValueChange = { newText ->
                    messageToSend = newText
                }
            )
            setButton()
        }
    }

    @Composable
    private fun setButton() {
        if (!isUploadStarted()) {
            Button(
                modifier = Modifier.wrapContentSize(),
                onClick = {
                    openFile()
                    receiver.sendMessage("DUBZGO")
                })
            { Text(text = "send File") }
        } else {
            Button(
                modifier = Modifier.wrapContentSize(),
                onClick = {
                    receiver.receiveFile()
                })
            { Text(text = "Receive File") }
        }

    }

    @Composable
    private fun getDevices() = receiver.devices.collectAsState(initial = null).value

    @Composable
    private fun connectionState() = receiver.connectionState.collectAsState(initial = false).value

    @Composable
    private fun isUploadStarted() = receiver.uploadStart.collectAsState(initial = false).value

    @Composable
    private fun GreetingView(name: String, onClick: (msg: String) -> Unit) {
        val msg = "Hello, $name"

        Card(
            backgroundColor = MaterialTheme.colors.primary,
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .clickable { onClick(msg) }
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Text(text = msg)
            }
        }
    }

    override fun onActivityResult(

        requestCode: Int, resultCode: Int, resultData: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, resultData)
        Log.v("MainActivity", "onActivityResult")

        if (requestCode == INTENT_IDENTIFIER
            && resultCode == Activity.RESULT_OK
        ) {
            resultData?.data?.also { uri ->
                Log.v("MainActivity", uri.path!!)
                receiver.sendFile(uri)
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        MyApplicationTheme {
            // Greeting("Android")
        }
    }
}