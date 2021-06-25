package com.gagapps.medboxsims.btServiceThread

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import java.io.IOException
import java.util.*

class ServerThread(bAdapter: BluetoothAdapter, appName: String, myuuid: UUID, tvStatus: TextView, context: Activity) : Thread() {
    val cntxt = context
    val tvStat = tvStatus
    lateinit var mSocket : BluetoothSocket

    private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE){
        bAdapter?.listenUsingInsecureRfcommWithServiceRecord(appName, myuuid)
    }

    override fun run() {
        var shouldLoop = true
        while (shouldLoop) {
            val socket: BluetoothSocket? = try {
                setStatus(cntxt, tvStat, "Listening")
                mmServerSocket?.accept()
            } catch (e: IOException) {
                Log.e("btDev", "Socket's accept() method failed", e)
                shouldLoop = false
                null
            }
            socket?.also {
                //manageMyConnectedSocket(it)
                //manage wether receive/send data
                setStatus(cntxt, tvStat, "Connected")
                Log.d("btDev",  "Server connected")
                mmServerSocket?.close()
                shouldLoop = false
            }
            mSocket = socket!!
        }
    }

    fun getSocket(): BluetoothSocket{
        return mSocket
    }

    fun setStatus(context: Activity, tv: TextView, msg: String){
        context.runOnUiThread {
            tv.text = msg
        }
    }

    fun cancel(){
        try {
            mmServerSocket?.close()
        }catch(e: IOException){
            e.printStackTrace()
            Log.e("btDev", "Could not close the connect socket ", e)
        }
    }
}