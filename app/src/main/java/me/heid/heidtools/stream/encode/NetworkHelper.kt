package me.heid.heidtools.stream.encode

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import me.heid.heidtools.stream.RTPpacket
import me.heid.heidtools.stream.junk.EncoderWrapper
import java.lang.ref.WeakReference
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress


class NetworkHelper(var port: Int,c: Context) {
    private val networkThread = NetworkThread(port)
    private val TAG = "NETWORK HELPER"
    private lateinit var hostAdress:String

    public fun start() {
        networkThread.start()
        networkThread.waitUntilReady()
    }

    public fun shutdown() {
        Log.d(EncoderWrapper.TAG, "releasing encoder objects")
        val handler = networkThread.getHandler()

        handler.sendMessage(handler.obtainMessage(NetworkThread.NetworkHandler.MSG_SHUTDOWN))
        try {
            networkThread.join()
        } catch (ie: InterruptedException ) {
            Log.w(EncoderWrapper.TAG, "Encoder thread join() was interrupted", ie)
        }
    }


    fun dispatch(ret:ByteArray){
        val handler = networkThread.getHandler()

        val message = handler.obtainMessage(
            NetworkThread.NetworkHandler.MSG_NEW_DATA_AVAILABLE
        )


        message.data.putByteArray(NetworkThread.NetworkHandler.KEY_DATA,ret)
        handler.sendMessage(message)

    }

    fun setAddress(hostAddress: String) {
        val handler = networkThread.getHandler()
        val message = handler.obtainMessage(
            NetworkThread.NetworkHandler.MSG_NEW_HOSTADRESS
        )
        message.data.putString(NetworkThread.NetworkHandler.KEY_DATA,hostAddress)
        handler.sendMessage(message)
        this@NetworkHelper.hostAdress = hostAddress

    }



    private class NetworkThread( val port:Int):Thread(){
        val TAG = "NETWORKHELPER/NETWORKTHREAD"

        private lateinit var hostAdress:String
        private var networkHandler : NetworkHandler? = null
        private val udpSocket = DatagramSocket()

        val mLock: Object = Object()

        @Volatile
        var mReady: Boolean = false

        init {

        }



        override fun run() {
            Looper.prepare()
            networkHandler =
                NetworkHandler(this)    // must create on encoder thread
            synchronized (mLock) {
                mReady = true
                mLock.notify()    // signal waitUntilReady()
            }
            Looper.loop()
            synchronized (mLock) {
                mReady = false
                networkHandler = null
            }
            Log.d(TAG, "looper quit")

            super.run()
        }

        public fun waitUntilReady() {
            synchronized (mLock) {
                while (!mReady) {
                    try {
                        mLock.wait()
                    } catch (ie: InterruptedException) { /* not expected */ }
                }
            }
        }


        fun getHandler() : NetworkHandler {
            synchronized (mLock) {
                // Confirm ready state.
                if (!mReady) {
                    throw RuntimeException("not ready")
                }
            }
            return networkHandler!!
        }

        override fun start() {
            super.start()
        }

        private fun newAddress(string: String) {
            hostAdress = string
        }

        val PT = 999// port
        var sequenceNr = 100
        var pid:Byte = 0

        private fun newData(byteArray: ByteArray) {
            if(byteArray.size> 66000){
                return
            }

            val p = RTPpacket(PT,sequenceNr,(sequenceNr++)*100,byteArray,byteArray.size)
            p.vp9header(0,0,0,1,1,0,0,pid++)
            if(pid == 127.toByte()){
                pid = 0
            }
            val header = p.header
            val ar =  header.copyOf(header.size+byteArray.size)
            System.arraycopy(byteArray,0,ar,header.size,byteArray.size)

            val packet = DatagramPacket(ar, ar.size,InetSocketAddress("192.168.0.35",999))
            Log.e(TAG,"SENT PACKAGE OF ${ar.size}")
            udpSocket.send(packet)
        }
        private fun shutdown() {
            Log.d(TAG, "shutdown")
            Looper.myLooper()!!.quit()
        }


        public class NetworkHandler(nt: NetworkThread): Handler(){
            private val mWeakNetworkThread = WeakReference<NetworkThread>(nt)
            companion object {
                val KEY_DATA = "keyData"
                val MSG_NEW_DATA_AVAILABLE: Int = 0
                val MSG_SHUTDOWN: Int = 1
                val MSG_NEW_HOSTADRESS: Int = 1
            }
            override fun handleMessage(msg: Message) {
                val what: Int = msg.what
                val networkThread = mWeakNetworkThread.get()
                if (networkThread == null) {
                    Log.w("TAG", "EncoderHandler.handleMessage: weak ref is null")
                    return
                }
                when (what) {
                   MSG_NEW_DATA_AVAILABLE -> networkThread.newData(
                       msg.data.getByteArray(
                           KEY_DATA
                       )!!
                   )
                    MSG_NEW_HOSTADRESS -> networkThread.newAddress(
                        msg.data.getString(KEY_DATA)!!
                    )
                    MSG_SHUTDOWN -> networkThread.shutdown()
                    else -> throw RuntimeException("unknown message " + what)
                }
            }
        }
    }
}