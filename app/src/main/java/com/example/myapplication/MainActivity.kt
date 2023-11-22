package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val TAG = MainActivity::class.simpleName
    private val INTENT_IDENTIFIER = 2
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        PermissionRequester(this).requestPermissions()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    setUIState()
                }
            }
        }
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    }

    @Composable
    private fun setUIState() {
        if (viewModel.connectionState()) { // sprawdza czy jesteśmy połączeni z innym urządzeniem
            Log.v(TAG, "connected")
            CreateConnectedUI()
        } else {
            Log.v(TAG, "scanner")
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

        val value = viewModel.getDevices()

        Log.v(TAG, value.toString())

        val size = if (value != null) value.deviceList!!.toList().size else 0
        val list = if (value != null) value.deviceList?.toList() else emptyList()
        if (viewModel.getDevices() != null) {
            CreateDiscoveredDevicesUI(size, list)
        } else {
            CreateStartScanUI()

        }
    }

    @Composable
    private fun CreateStartScanUI() {
        val interactionSource = remember { MutableInteractionSource() }
        Column(
            Modifier
                .padding(20.dp)
                .wrapContentSize()
                .clickable(
                    indication = null, interactionSource = interactionSource
                ) { viewModel.startScan() })
        {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally)

            ) {
                Image(
                    painter = painterResource(id = R.mipmap.magnyfying_foreground),
                    contentDescription = null,
                    modifier = Modifier.wrapContentSize(),
                    colorFilter = ColorFilter.tint(Color(120, 120, 150))
                )

            }
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Tap to start scan",
                    fontSize = 20.sp,
                    color = Color(100, 100, 120)
                )

            }
        }
    }

    @Composable
    private fun CreateDiscoveredDevicesUI(
        size: Int,
        list: List<WifiP2pDevice>?
    ) {
        Column {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 10.dp),
                fontSize = 20.sp,
                textAlign = TextAlign.End,
                text = "Select device to connect",
                color = Color(100, 100, 120)

            )

            LazyColumn(modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp)) {
                items(size) { number ->
                    addItems(list?.get(number)?.deviceName) {
                        list?.get(number)?.let { viewModel.connect(it) }
                    }
                }
            }
        }
    }

    @Composable
    private fun CreateConnectedUI() {
        val interactionSource = remember { MutableInteractionSource() }
        if (!viewModel.isUploadStarted()) {
            SendingFileUI(interactionSource)
        } else {
            ReceivingFileUI(interactionSource)
        }
    }

    @Composable
    private fun ReceivingFileUI(interactionSource: MutableInteractionSource) {
        Column(modifier = Modifier.run {
            wrapContentSize()
                .clickable(
                    indication = null, interactionSource = interactionSource
                ) { viewModel.receiveFile() }
                .padding(8.dp)
        }) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally)
            ) {
                addArrowImage(false)
            }
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally)
                    .padding(20.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    textAlign = TextAlign.Center,
                    text = "Click to receive file",
                    fontSize = 20.sp,
                    color = Color(100, 100, 120)
                )

            }
        }
    }

    @Composable
    private fun SendingFileUI(interactionSource: MutableInteractionSource) {
        Column(modifier = Modifier.run {
            wrapContentSize()
                .clickable(
                    indication = null, interactionSource = interactionSource
                ) { openFile() }
                .padding(8.dp)
        }) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .align(alignment = Alignment.CenterHorizontally)
            ) {
                addArrowImage(true)

            }
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally)
                    .padding(20.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    textAlign = TextAlign.Center,
                    text = "Click to send file",
                    fontSize = 20.sp,
                    color = Color(100, 100, 120)
                )
            }
        }
    }


    @Composable
    private fun addArrowImage(upsideDown: Boolean) {
        Image(
            painter = painterResource(id = R.mipmap.szczalki_foregroud),
            contentDescription = null,
            modifier = Modifier
                .wrapContentSize()
                .rotate(if (upsideDown) 270F else 90F),
            colorFilter = ColorFilter.tint(Color(120, 120, 150))
        )
    }

    @Composable
    private fun addItems(name: String?, onClick: () -> Unit) {

        Card(
            backgroundColor = Color(120, 120, 150),
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .clickable { onClick() }) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Text(text = if (!name.isNullOrEmpty()) name else "")
            }
        }
    }

    override fun onActivityResult(

        requestCode: Int, resultCode: Int, resultData: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, resultData)
        Log.v("MainActivity", "onActivityResult")

        if (requestCode == INTENT_IDENTIFIER && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                viewModel.sendMessage("DUBZGO")

                Log.v("MainActivity", uri.path!!)
                viewModel.sendFile(uri)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @Preview
    @Composable
    fun CreateScanerUIPrv() {
        CreateScanerUI()
    }

    @Preview
    @Composable
    fun CreateConnectedUIPrv() {
        CreateConnectedUI()
    }


}