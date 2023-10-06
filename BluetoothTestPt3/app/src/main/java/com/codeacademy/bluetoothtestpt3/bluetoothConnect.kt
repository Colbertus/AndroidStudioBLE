package com.codeacademy.bluetoothtestpt3

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import java.util.UUID

class BluetoothGattManager(val context: Context) {


    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val deviceAddress = "38:AB:41:3C:D4:34"
    val serviceUUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    val characteristicUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)

    init {
        val gattCallback = object : BluetoothGattCallback() {
            @SuppressLint("MissingPermission")
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // Connection established, now discover services
                    gatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // Handle disconnect
                }
            }

            @SuppressLint("MissingPermission")
            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt?.getService(serviceUUID)
                    val characteristic = service?.getCharacteristic(characteristicUUID)
                    gatt?.readCharacteristic(characteristic)
                    val data = "Test".toByteArray()
                    characteristic?.value = data
                    gatt?.writeCharacteristic(characteristic)
                } else {
                    // Handle service discovery failure
                }
             }
            }
        }
    }
    fun connectToDevice(device: BluetoothDevice) {
       // bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

