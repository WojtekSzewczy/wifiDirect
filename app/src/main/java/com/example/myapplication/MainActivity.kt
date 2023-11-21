package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var receiver: WifiBroadcastReceiver
    private val INTENT_IDENTIFIER = 2
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

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

        if (viewModel.connectionState()) { // sprawdza czy jesteśmy połączeni z innym urządzeniem
            Log.v(TAG, "connected")
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
        val value = viewModel.getDevices()
        Log.v(TAG, value.toString())

        val size = if (value != null) value.deviceList!!.toList().size else 0
        val list = if (value != null) value.deviceList?.toList() else emptyList()
        Column {
            Button(onClick = {
                viewModel.startScan()
                Log.v(TAG, "DUPA")
            }) { Text(text = "start scan") } //TODO start scan po bożemu
            LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
                items(size) { number ->
                    GreetingView(name = list?.get(number)?.deviceAddress + list?.get(number)?.deviceName) {
                        list?.get(number)?.let { viewModel.connect(it) }
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
        if (!viewModel.isUploadStarted()) {
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
                    viewModel.receiveFile()
                })
            { Text(text = "Receive File") }
        }

    }


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
                viewModel.sendMessage("DUBZGO")

                Log.v("MainActivity", uri.path!!)
                viewModel.sendFile(uri)
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