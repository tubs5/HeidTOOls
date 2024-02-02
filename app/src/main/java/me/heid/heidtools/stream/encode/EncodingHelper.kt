package me.heid.heidtools.stream.encode

import android.media.*
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Surface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.*

class EncodingHelper(
    frameRate: Int, width: Int, height: Int, formatIn: String,
    outputFile: File,
    val networkHelper: NetworkHelper
) {
    val codec :MediaCodec
    private val format:MediaFormat
    private var inputSurface:Surface
    val TAG = "ENCODINGHELPER"
    private var encoderThread: EncoderThread


    public fun getInputSurface(): Surface {
        return inputSurface
    }

    init {
        format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_VP9,width,height);
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
           // MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
           // MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1_000_000) //1Mb/s
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 90)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        val codecName = MediaCodecList(MediaCodecList.ALL_CODECS).findEncoderForFormat(format)
        Log.w(TAG,"CODEC NAME: $codecName")
        codec = MediaCodec.createByCodecName(codecName);
        Log.w(TAG,"CODEC INFO: ${codec.codecInfo}")
        codec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoderThread = EncoderThread(codec, outputFile,90,networkHelper)
        inputSurface = codec.createInputSurface()


    }

    fun start(){
        codec.start()

        // Start the encoder thread last.  That way we're sure it can see all of the state
        // we've initialized.
        encoderThread.start()
        encoderThread.waitUntilReady()
    }

    /**
     * Shuts down the encoder thread, and releases encoder resources.
     * <p>
     * Does not return until the encoder thread has stopped.
     */
    public fun shutdown() {
        Log.d(TAG, "releasing encoder objects")

        val handler = encoderThread.getHandler()
        handler.sendMessage(handler.obtainMessage(EncoderThread.EncoderHandler.MSG_SHUTDOWN))
        try {
            encoderThread.join()
        } catch (ie: InterruptedException ) {
            Log.w(TAG, "Encoder thread join() was interrupted", ie)
        }

        codec.stop()
        codec.release()
    }

    /**
     * Notifies the encoder thread that a new frame is available to the encoder.
     */
    public fun frameAvailable() {
        val handler = encoderThread.getHandler()
        handler.sendMessage(handler.obtainMessage(
            EncoderThread.EncoderHandler.MSG_FRAME_AVAILABLE
        ))
    }

    public fun waitForFirstFrame() {
        encoderThread.waitForFirstFrame()
    }


    private class EncoderThread(mediaCodec:MediaCodec,outputFile: File, orientationHint:Int,val networkHelper: NetworkHelper):Thread(){
        val TAG = "ENCODERHELPER/THREAD"
        val mEncoder = mediaCodec
        val mBufferInfo = MediaCodec.BufferInfo()
        var fos:FileOutputStream
        var outputStream: ByteArrayOutputStream = ByteArrayOutputStream()
        var mEncodedFormat: MediaFormat? = null
        @Volatile
        var mReady: Boolean = false
        var mHandler: EncoderHandler? = null
        var mFrameNum: Int = 0
        val mLock: Object = Object()

        var sps: ByteArray? = null
        var pps:ByteArray? = null



        init {
            val f = File(outputFile.parent,"${System.currentTimeMillis()}(TEST).mp4")
            fos = FileOutputStream(f)
            Log.e(TAG,"TEST FILE :${f.name}")
        }



        public override fun run() {
            Looper.prepare()
            mHandler = EncoderHandler(this)    // must create on encoder thread
            synchronized (mLock) {
                mReady = true
                mLock.notify()    // signal waitUntilReady()
            }

            Looper.loop()

            synchronized (mLock) {
                mReady = false
                mHandler = null
            }
            Log.d(TAG, "looper quit")
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
        public fun waitForFirstFrame() {
            synchronized (mLock) {
                while (mFrameNum < 1) {
                    try {
                        mLock.wait()
                    } catch (ie: InterruptedException) {
                        ie.printStackTrace();
                    }
                }
            }
            Log.d(TAG, "Waited for first frame");
        }

        public fun drainEncoder(): Boolean {
            val TIMEOUT_USEC: Long = 0     // no timeout -- check for buffers, bail if none
            var encodedFrame = false

            while (true){
                var encoderStatus: Int = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC)
                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    break;
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // Should happen before receiving buffers, and should only happen once.
                    // The MediaFormat contains the csd-0 and csd-1 keys, which we'll need
                    // for MediaMuxer.  It's unclear what else MediaMuxer might want, so
                    // rather than extract the codec-specific data and reconstruct a new
                    // MediaFormat later, we just grab it here and keep it around.
                    mEncodedFormat = mEncoder.getOutputFormat()
                   // sps = mEncodedFormat!!.getByteBuffer("csd-0")!!.array()
                   // pps = mEncodedFormat!!.getByteBuffer("csd-1")!!.array()



                    Log.d(TAG, "  " + mEncodedFormat)
//                    Log.d(TAG,"CSD-0 KEY: " + Arrays.toString(mEncodedFormat!!.getByteBuffer("csd-0")!!.array()))
  //                  Log.d(TAG,"CSD-1 KEY: " + Arrays.toString(mEncodedFormat!!.getByteBuffer("csd-1")!!.array()))
                } else if (encoderStatus < 0) {
                    Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                            encoderStatus)
                    // let's ignore it
                } else {
                    var encodedData: ByteBuffer? = mEncoder.getOutputBuffer(encoderStatus)
                    if (encodedData == null) {
                        throw RuntimeException("encoderOutputBuffer " + encoderStatus +
                                " was null");
                    }

                    val outData = ByteArray(mBufferInfo.size)
                    encodedData[outData]

                    //Log.e(TAG, (outData).contentToString())

                    outputStream.write(outData)

                    mEncoder.releaseOutputBuffer(encoderStatus, false)
                    encodedFrame = true


                }
            }

            var ret = outputStream.toByteArray()
/* if (ret.size > 5 && ret[4].toInt() == 0x65) //key frame need to add sps pps
 {
     outputStream.reset()
     outputStream.write(1)
     outputStream.write(sps)
     outputStream.write(pps)
     outputStream.write(ret)
     ret = outputStream.toByteArray()
 }*/
 outputStream.reset()
 networkHelper.dispatch(ret)
 //fos.write(ret)

 return encodedFrame
}




fun frameAvailable() {
 if (drainEncoder()) {
     synchronized (mLock) {
         mFrameNum++
         mLock.notify()
     }
 }
}
fun shutdown() {
 Log.d(TAG, "shutdown")
 Looper.myLooper()!!.quit()
 fos.close()
}


public fun getHandler(): EncoderHandler {
 synchronized (mLock) {
     // Confirm ready state.
     if (!mReady) {
         throw RuntimeException("not ready")
     }
 }
 return mHandler!!
}

public class EncoderHandler(et: EncoderThread): Handler() {
 var mHandler: EncoderHandler? = null
 val TAG = "ENCODINGHELPER/THREAD/HANDLER"
 companion object {
     val MSG_FRAME_AVAILABLE: Int = 0
     val MSG_SHUTDOWN: Int = 1
 }

 // This shouldn't need to be a weak ref, since we'll go away when the Looper quits,
 // but no real harm in it.
 private val mWeakEncoderThread = WeakReference<EncoderThread>(et)

 // runs on encoder thread
 public override fun handleMessage(msg: Message) {
     val what: Int = msg.what

     val encoderThread: EncoderThread? = mWeakEncoderThread.get()
     if (encoderThread == null) {
         Log.w(TAG, "EncoderHandler.handleMessage: weak ref is null")
         return
     }

     when (what) {
         MSG_FRAME_AVAILABLE -> encoderThread.frameAvailable()
         MSG_SHUTDOWN -> encoderThread.shutdown()
         else -> throw RuntimeException("unknown message " + what)
     }
 }
}
}
}


