
package com.codeacademy.bluetoothtestpt3

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.codeacademy.bluetoothtestpt3.ui.theme.BluetoothTestPt3Theme
import java.util.UUID


// Most of the code needed for bluetooth communication is in the main activity so it can perform the code on startup
class MainActivity : ComponentActivity() {

    // Since this code uses some newer methods, it does need a higher API level and a check for that API
    @RequiresApi(Build.VERSION_CODES.S)

    override fun onCreate(savedInstanceState: Bundle?) {

        // The below values are the codes for the specific bluetooth permissions needed for this app
        val REQUEST_BLUETOOTH_PERMISSIONS = 1
        val BLUETOOTH_CONNECT_PERMISSION_REQUEST = 123

        // Also created values for the bluetooth permissions needed for this app
        val bluetoothPermission = Manifest.permission.BLUETOOTH
        val bluetoothAdminPermission = Manifest.permission.BLUETOOTH_ADMIN

        // Used with the values above for the code right below to check for the specific permissions
        val hasBluetoothPermission = ContextCompat.checkSelfPermission(this, bluetoothPermission) == PackageManager.PERMISSION_GRANTED
        val hasBluetoothAdminPermission = ContextCompat.checkSelfPermission(this, bluetoothAdminPermission) == PackageManager.PERMISSION_GRANTED

        // If the permissions are not granted, then the app will request them
        if (!hasBluetoothPermission || !hasBluetoothAdminPermission) {
            // Request Bluetooth permissions if not granted
            ActivityCompat.requestPermissions(this, arrayOf(bluetoothPermission, bluetoothAdminPermission), REQUEST_BLUETOOTH_PERMISSIONS)
        } else {
            // You have the required permissions, you can proceed with Bluetooth operations
            // For example, you can initiate readCharacteristic and writeCharacteristic here
        }

        super.onCreate(savedInstanceState)

        // Link to the bluetooth manager syntax: [link](https://developer.android.com/reference/android/bluetooth/BluetoothManager)
        fun Context.bluetoothAdapter(): BluetoothAdapter? =
            (this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        // The below values are the MAC address of the tablet, the UUID of the specific service we will be writing to, and the specific characteristic we will be writing to as well
        val deviceAddress = "38:AB:41:3C:D4:34"
        val serviceUUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
        val characteristicUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

        // Initializes the device with the bluetooth adapter and the MAC address
        val device = bluetoothAdapter()?.getRemoteDevice(deviceAddress)

        // The gatt callback is used to handle the connection and the data transfer including when the connection is established, when the services are discovered, and when the characteristic is written to
        // The link to BluetoothGattCallback() syntax: [link](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback)
        val gattCallback = object : BluetoothGattCallback() {

            // The below code is used to check when the GATT client has connected or disconnected from the GATT server
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {

                    // The below log is used when the tablet is connected to the bluetooth module
                    Log.d(TAG, "Connected to the GATT server!")

                    // The permission necessary for performing gatt functions
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED) {
                        gatt?.discoverServices()
                    } else {
                        // Permission is not granted, request it.
                        /*ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                            BLUETOOTH_CONNECT_PERMISSION_REQUEST
                        ) */
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "Disconnected from the GATT server!")
                }
            }

            // The callback function gets performed when the GATT client has discovered the services on the GATT server
            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Services discovered!")
                    val service = gatt?.getService(serviceUUID)
                    val characteristic = service?.getCharacteristic(characteristicUUID)
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED) {
                        gatt?.readCharacteristic(characteristic)
                    } else {
                        // Permission is not granted, request it.
                        /* ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                            BLUETOOTH_CONNECT_PERMISSION_REQUEST
                        ) */
                    }
                } else {
                   Log.d(TAG, "No services discovered!")
                }
            }

            // When a characteristic is changed, the callback function gets performed. So if anything is written to the characteristic, then the below callback gets performed
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
            Log.d(TAG, "Characteristic changed!")
            }

            // When a characteristic gets written to, the callback function gets performed
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Success!")
                } else {
                    Log.d(TAG, "Failure!")
                }
            }

        }

        // setContent is the main function that is used to set the content of the app
        setContent {

            // This function is supposed to write a test string to the specific characteristic declared earlier
            fun writeToCharacteristic() {
                val bluetoothGatt = device?.connectGatt(this, false, gattCallback)
                val service = bluetoothGatt?.getService(serviceUUID)
                val characteristic = service?.getCharacteristic(characteristicUUID)
                val data = "Test".toByteArray()
                @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                if (characteristic != null) {
                        bluetoothGatt.writeCharacteristic(characteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_SIGNED)
                        Log.d("Bluetooth", "Wrote to characteristic!")
                } else {
                    Log.d("Bluetooth", "Failed to write to characteristic!")
                }
            }

            // The below code is used to check if the device has bluetooth LE capabilities, returns a boolean
            val bluetoothLeAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

            // The function call to actually write to bluetooth characteristic
            writeToCharacteristic()

            // Checks if bluetooth LE is available and logs it
            if(bluetoothLeAvailable == true) {
                Log.d("Bluetooth", "Bluetooth LE is available")
            } else {
                Log.d("Bluetooth", "Bluetooth LE is not available")
            }

            val context = LocalContext.current
            // BluetoothGattManager(context)
            BluetoothTestPt3Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red)
    ) {
        Button(
            onClick = {
                val data = "Test".toByteArray()
            },
            modifier = Modifier
                .size(75.dp)
                .offset(y = 200.dp)
        ) {

        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BluetoothTestPt3Theme {
        Greeting("Android")
    }
}