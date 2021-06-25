package com.gagapps.medboxsims

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.gagapps.medboxsims.btServiceThread.SendReceive
import com.gagapps.medboxsims.btServiceThread.ServerThread
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    lateinit var bAdapter: BluetoothAdapter
    lateinit var tvStatus: TextView
    lateinit var servThread: ServerThread
    private val MY_UUID = UUID.fromString("91ce3659-1535-4b05-a89d-08ca023c8dd5")
    private val MY_APP_NAME = "medAdh"
    private val REQUEST_CODE_ENABLE_BT: Int = 1
    private val FINE_LOCATION_PERMISSION_REQUEST: Int = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            bAdapter = BluetoothAdapter.getDefaultAdapter()
            btSwitch.setOnCheckedChangeListener(this)
            bt_listen.setOnClickListener {
                startListen()
            }
            bt_imatinib.setOnClickListener {
                sendData("Imatinib")
            }

            bt_other.setOnClickListener {
                sendData("Other")
            }

            allowLocationDetectionPermissions()
            tvStatus = findViewById(R.id.tv_status)

        }catch (e: NullPointerException){
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_LONG).show()
        }

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked){
            btEnable()
        }
        else{
            btDisable()
        }
    }

    private fun btEnable(){
        if (bAdapter.isEnabled){
            Toast.makeText(this, "Bluetooth is already enable", Toast.LENGTH_SHORT).show()
        }
        else {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BT)
            tvStatus.text = "BT ON"
        }
    }//end func

    private fun btDisable(){
        if (!bAdapter.isEnabled){
            Toast.makeText(this, "Bluetooth is already disable", Toast.LENGTH_SHORT).show()
        }
        else {
            bAdapter.disable()
            tv_status.text = "BT OFF"
        }
    }//end func

    private fun allowLocationDetectionPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_PERMISSION_REQUEST)
        }
    }

    private fun startListen(){
        if (!bAdapter.isEnabled)
            Toast.makeText(this, "Please Turn On the Bluetooth", Toast.LENGTH_SHORT).show()
        else{
            servThread = ServerThread(bAdapter, MY_APP_NAME, MY_UUID, tvStatus, this)
            servThread.start()
        }
    }

    private fun sendData(data: String){
        val dataConv = data.toByteArray()
        try {
            val socket = servThread.getSocket()
            socket?.let {
                val handler = Handler(Looper.getMainLooper())
                val services = SendReceive(socket, handler)
                services.write(dataConv)
            }
            Log.d("btDev", "Send data success!")
        }catch (e: IOException){
            e.printStackTrace()
            Log.e("btDev", "Send data error")
        }

    }


}