package me.heid.heidtools.stream.encode

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.*
import android.hardware.camera2.CameraDevice.StateCallback
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.*
import android.util.Log
import android.view.Surface
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import kotlinx.android.synthetic.main.activity_cam_stream.*
import me.heid.heidtools.R
import me.heid.heidtools.stream.chat.WifiDirectHelper
import java.io.File
import java.util.*


class CamStream : AppCompatActivity() {
    private lateinit var session: CameraCaptureSession
    val tag = "CamStream"

    lateinit var camHandler:Handler
    lateinit var encodingHelper: EncodingHelper
    lateinit var networkHelper: NetworkHelper
    lateinit var manager: CameraManager
    lateinit var cameraID:String
    var recordingStarted = false
    lateinit var file2:File

    override fun onCreate(savedInstanceState: Bundle?) {
        file2 = File(applicationContext.filesDir,"${System.currentTimeMillis()}.mp4")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cam_stream)


        buttonCamStreamStart.setOnClickListener(object : OnClickListener{
            override fun onClick(v: View?) {
                if(!recordingStarted){
                    encodingHelper.start()
                    recordingStarted = true
                }
            }

        })
        buttonCamStreamStop.setOnClickListener(object: OnClickListener{
            override fun onClick(v: View?) {
                encodingHelper.waitForFirstFrame()
                recordingStarted = false
                session.stopRepeating()
                encodingHelper.shutdown()
            }

        })

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val player: ExoPlayer = ExoPlayer.Builder(this).build()
        player.setVideoSurfaceView(surfaceView)
        val firstItem: MediaItem = MediaItem.fromUri("udp://192.168.0.30:999/")
        player.addMediaItem(firstItem)



        player.prepare()
        player.play()


    }

    override fun onStart() {
        super.onStart()
        manager = getSystemService(CAMERA_SERVICE) as CameraManager
        if(!recordingStarted){
            CameraSelector(manager)
            initNetwork()
        }
    }

    private fun initNetwork() {
        networkHelper = NetworkHelper(999, applicationContext)

        val asHost = object : WifiDirectHelper.OnConnectionAsHost {
            override fun OnConnectionAsHost(HostAddress: String?) {
                if (HostAddress != null) {
                    networkHelper.setAddress(HostAddress)
                } else Log.e(tag, "NO HOST ADRESS")
            }

        }
        val asUser = object : WifiDirectHelper.OnConnectionAsUser {
            override fun OnConnectionAsUser(HostAddress: String?) {
                if (HostAddress != null) {
                    networkHelper.setAddress(HostAddress)
                } else Log.e(tag, "NO HOST ADRESS")
            }

        }
        networkHelper.start()
        val wifiHelper = WifiDirectHelper(applicationContext, asHost, asUser)
    }
    fun initEncoder(it: CameraConfigDialog.Configurations){
        encodingHelper = EncodingHelper(
            it.maxFps, it.width, it.height,it.format, File(
                applicationContext.filesDir,
                "${System.currentTimeMillis()}.mp4"
            ), networkHelper
        )
    }
    fun initCamera() {
        manager = getSystemService(CAMERA_SERVICE) as CameraManager
        //cameraID = CameraSelector(manager)
        val camThread = HandlerThread(tag).apply { start() }
        camHandler = Handler(camThread.looper)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(tag, "NO CAMERA PERMISSION")
            //TODO ASK FOR CAM PERMISSION
            return
        }
        manager.openCamera(cameraID, camCallback, camHandler);

    }
    val camCallback = object : StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.w(tag, "Camera Opened")

            val builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            builder.addTarget(encodingHelper.getInputSurface())


            camera.createCaptureSession(
                ArrayList<Surface>().apply { add(encodingHelper.getInputSurface()) },
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        session.setRepeatingRequest(builder.build(),camCaptureCallback,camHandler)
                        this@CamStream.session = session


                       //##### session.capture(builder.build(), camCaptureCallback, camHandler)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        val exc =
                            RuntimeException("Camera ${camera.id} session configuration failed")
                        Log.e(tag, exc.message, exc)
                    }

                },
                camHandler
            )


        }

        val camCaptureCallback = object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureStarted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                timestamp: Long,
                frameNumber: Long
            ) {
                super.onCaptureStarted(
                    session,
                    request,
                    timestamp,
                    frameNumber
                )
            }

            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
                if (isCurrentlyRecording()) {
                    encodingHelper.frameAvailable()
                }
            }
            private fun isCurrentlyRecording(): Boolean {
                return recordingStarted
            }
            override fun onCaptureFailed(
                session: CameraCaptureSession,
                request: CaptureRequest,
                failure: CaptureFailure
            ) {
                super.onCaptureFailed(session, request, failure)
                Log.e(tag, "CAPTURED FAILED")
            }

        }


        override fun onDisconnected(camera: CameraDevice) {
            Log.w(tag, "Camera Disconected")
        }

        override fun onError(camera: CameraDevice, error: Int) {
            val msg = when (error) {
                ERROR_CAMERA_DEVICE -> "Fatal (device)"
                ERROR_CAMERA_DISABLED -> "Device policy"
                ERROR_CAMERA_IN_USE -> "Camera in use"
                ERROR_CAMERA_SERVICE -> "Fatal (service)"
                ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                else -> "Unknown"
            }
            Log.e(tag, "Camera ERROR: $msg")
        }

    }
    private fun CameraSelector(manager: CameraManager): String {
        //TODO: CAMERA SELECTOR AND CONFIG


        cameraID = manager.cameraIdList[0]

        val characteristics: CameraCharacteristics =
            manager.getCameraCharacteristics(cameraID)
        val configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) as StreamConfigurationMap
        val d = CameraConfigDialog(configs.toString(), function = {
            initEncoder(it)
            initCamera()
        })
        d.show(this.supportFragmentManager,"CameraSelector")
        for (c in manager.getCameraCharacteristics(cameraID).keys) {
            var r = manager.getCameraCharacteristics(cameraID).get(c)
            if (r is Array<*>) {
                Log.v(tag, "$c \t ${Arrays.toString(r)}")
            } else if (r is IntArray) {
                Log.v(tag, "$c \t ${Arrays.toString(r)}")
            } else {
                Log.v(tag, "$c \t ${r!!::class.simpleName} ${r.toString()}")
            }
        }
        return cameraID
    }




}