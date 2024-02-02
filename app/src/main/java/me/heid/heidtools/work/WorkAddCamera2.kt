package me.heid.heidtools.work

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PermissionGroupInfo
import android.content.pm.PermissionInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.Surface.ROTATION_0
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.google.android.gms.tasks.OnSuccessListener
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.android.synthetic.main.fragment_work_add_camera2.*
import kotlinx.android.synthetic.main.fragment_work_view.*
import me.heid.heidtools.BuildConfig
import me.heid.heidtools.R
import me.heid.heidtools.stream.encode.CameraConfigDialog
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.util.*
import java.util.concurrent.Executor


class WorkAddCamera2 : Fragment() {
    private lateinit var keyUri: Uri
    private lateinit var extraUri: Uri
    private lateinit var dashUri: Uri
    private lateinit var basePath: File
    private lateinit var camControl:CameraXCControl

    class Ocr{
        internal fun ocrBasic(it: Bitmap, onSuccessListener: OnSuccessListener<Text>) {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val impImage = InputImage.fromBitmap(it, 0)

            recognizer.process(impImage)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }


        internal fun checkKeyText(visionText: Text): String {
            var text: String
            for (blocks in visionText.textBlocks) {
                for (line in blocks.lines)
                    for (elements in line.elements) {
                        text = elements.text.lowercase()
                        if (text.contains("se")) {
                            text = text.replace('o', '0')
                            if(text.contains("se\\d{5}") && text.length >= 7) {
                                val start = text.indexOf("se")
                                text = text.substring(start, start+7 )
                                return text
                            }
                        }

                    }
            }
            return ""
        }

        internal fun checkDashText(visionText: Text): String {
            var text: String
            val texts = ArrayList<Int>()
            for (blocks in visionText.textBlocks) {
                for (line in blocks.lines)
                    for (elements in line.elements) {
                        text = elements.text.lowercase()

                        text = text.replace('o', '0')
                        var number = ""
                        text.toCharArray().forEach {
                            if (it.isDigit()) number += it
                        }
                        try{
                            if (number.isNotEmpty()) texts.add(number.toInt())
                        }catch (e:java.lang.NumberFormatException){
                            e.printStackTrace()
                        }



                    }
            }
            var greatest = 0
            texts.forEach {
                if (it > greatest) greatest = it
            }

            return "$greatest km"
        }
    }

    class CameraXCControl(val fragment: Fragment){
        private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
        private lateinit var cameraSelector: CameraSelector
        private lateinit var camera : Camera

        private val cameraExecutor = Executor{
            it.run()
        }
        val imageCapture = ImageCapture.Builder()
            .setTargetRotation(ROTATION_0)
            .build()
        fun onCreate() {
            cameraProviderFuture = ProcessCameraProvider.getInstance(fragment.requireContext())
        }

        fun onViewCreated(view:PreviewView,){

            val requestPermissionLauncher =
                fragment.registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        // Permission is granted. Continue the action or workflow in your
                        // app.
                    } else {
                        // Explain to the user that the feature is unavailable because the
                        // feature requires a permission that the user has denied. At the
                        // same time, respect the user's decision. Don't link to system
                        // settings in an effort to convince the user to change their
                        // decision.
                    }
                }



            when {
                ContextCompat.checkSelfPermission(
                    fragment.requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                }else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissionLauncher.launch(
                        Manifest.permission.CAMERA

                    )
                }
            }



            cameraProviderFuture.addListener(Runnable {
                val cameraProvider = cameraProviderFuture.get()
                bindPhoto(cameraProvider,view)
            }, ContextCompat.getMainExecutor(fragment.requireContext()))

        }

        private fun bindPhoto(cameraProvider: ProcessCameraProvider, view: PreviewView) {

            val preview : Preview = Preview.Builder()
                .build()

            cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            preview.setSurfaceProvider(view.surfaceProvider)


            val imageAnalysis = null
            camera = cameraProvider.bindToLifecycle(fragment, cameraSelector, imageCapture,
                preview)
        }

        fun takePhoto(path:String,lmbd: (result: ImageCapture.OutputFileResults, path:String) -> Unit){
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(File(path)).build()
            imageCapture.takePicture(outputFileOptions, cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(error: ImageCaptureException)
                    {
                        error.printStackTrace()
                        Toast.makeText(fragment.requireContext(),"Picture Failed",Toast.LENGTH_LONG).show()
                    }
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        lmbd(outputFileResults,path)
                    }
                })

        }


    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)


        camControl = CameraXCControl(this)
        camControl.onCreate()

        val path = arguments?.getString("path")

        if(path == null){
            Toast.makeText(context,"MAKING A NEW ENTRY",Toast.LENGTH_SHORT).show()

            val cd = LocalDate.now()
            val date = "${cd.year}-${cd.monthValue}-${cd.dayOfMonth}"

            val filepath = (date).replace("-", "/")
            val file = File(context?.filesDir?.path, "data/$filepath")
            var highestnr = 0
            if(!file.exists()){
                file.mkdirs()
            }
            for(f in file.listFiles()!!){
               val i =  f.name.substring(3).toInt()
                if(i > highestnr){
                    highestnr = i;
                }
            }
            highestnr = highestnr.inc()
            basePath = File(file, "car$highestnr")
            File(basePath, "Extra").mkdirs()





        }else{
            Toast.makeText(context,"ENTRY FOUND",Toast.LENGTH_SHORT).show()
            basePath = File(path)
        }

         keyUri = FileProvider.getUriForFile(
            this.requireContext(),
            BuildConfig.APPLICATION_ID + ".provider",
            File(basePath, "Key.jpeg")
        )
         dashUri = FileProvider.getUriForFile(
            this.requireContext(),
            BuildConfig.APPLICATION_ID + ".provider",
            File(basePath, "Dash.jpeg")
        )
         extraUri = FileProvider.getUriForFile(
            this.requireContext(),
            BuildConfig.APPLICATION_ID + ".provider",
            File(basePath, "Extra")
        )

        //imageUri = File(basePath,"Key.jpeg").toUri()
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val b = Bundle()
            b.putString("path",path)
            parentFragmentManager.beginTransaction().setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, WorkBrowse::class.java,b ).commit()
        }



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_work_add_camera2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        camControl.onViewCreated(workAddSurface)
        if(arguments?.getString("path") != null){
            loadData()
        }



        WorkAddKeyPhoto.setOnClickListener {
           camControl.takePhoto(basePath.path+"/Key.jpeg") { result, path ->
               requireActivity().runOnUiThread {
                   val b = BitmapFactory.decodeFile(path)
                   val ocr = Ocr()
                   ocr.ocrBasic(b){
                       val text2 = ocr.checkKeyText(it)
                       WorkAddKeyInfoText.text = text2
                       WorkAddKeyInfoText.invalidate()
                   }
                   WorkAddKeyPhoto.setImageBitmap(b)
               }


           }
        }
        WorkAddDashPhoto.setOnClickListener {
            camControl.takePhoto(basePath.path+"/Dash.jpeg") { result, path ->
                requireActivity().runOnUiThread {
                    val b = BitmapFactory.decodeFile(path)
                    val ocr = Ocr()
                    ocr.ocrBasic(b){
                        val text2 = ocr.checkDashText(it)
                        workAddDashInfoText.text = text2
                        workAddDashInfoText.invalidate()
                    }

                    WorkAddDashPhoto.setImageBitmap(b)
                }
            }
        }
        workAddExtraPhotos.setOnClickListener {
            camControl.takePhoto(basePath.path+"/Extra/"+workAddExtraInfoText.text.toString() + ".jpeg") { result, path ->
                requireActivity().runOnUiThread {
                    workAddExtraInfoText.text = (workAddExtraInfoText.text.toString().toInt()+1).toString()
                    val b = BitmapFactory.decodeFile(path)
                    workAddExtraPhotos.setImageBitmap(b)
                }
            }
        }
        workAddFloatingButton.setOnClickListener {
            this@WorkAddCamera2.requireActivity().onBackPressed()
        }
    }

    private fun loadData() {
        if(File(basePath,"Key.jpeg").exists()) {
            val keyImage = BitmapFactory.decodeFile(basePath.path+"/Key.jpeg")
            WorkAddKeyPhoto.setImageBitmap(keyImage)
            WorkAddKeyPhoto.invalidate()
        }
        if(File(basePath,"Dash.jpeg").exists()) {
            val dashImage = BitmapFactory.decodeFile(basePath.path+"/Dash.jpeg")
            WorkAddDashPhoto.setImageBitmap(dashImage)
            WorkAddDashPhoto.invalidate()
        }
        if(File(basePath,"Extra").isDirectory){
            val ExtraPhoto = BitmapFactory.decodeFile(basePath.path+"/Extra/0.jpeg")
            workAddExtraPhotos.setImageBitmap(ExtraPhoto)
            workAddExtraPhotos.invalidate()
            workAddExtraInfoText.text = File(basePath,"Extra").listFiles().size.toString()


        }
        val dataFile = File(basePath, "data.json")
        if(dataFile.exists()){
            val text = File(basePath.path+"/data.json").readText()
            if(text.isNotEmpty()){
                val json = JSONObject(text)
                 if(json.has("Text")){
                     workAddExtraEditText.setText(json.getString("Text"))
                }
                if(json.has("Serial")){
                    WorkAddKeyInfoText.setText(json.getString("Serial"))
                }
                if(json.has("Km")){
                    workAddDashInfoText.setText(json.getString("Km"))
                }
            }
            Log.e("TAG",File("${basePath.path}/data.json").path)
        }

    }

    private fun saveData() {
        val dataFile = File(basePath, "data.json")
        val json = if (dataFile.exists()) {
            JSONObject(dataFile.readText())
        } else JSONObject()
        json.apply {
            put("Text", workAddExtraEditText.text.toString())
            put("Km", workAddDashInfoText.text)
            put("Serial", WorkAddKeyInfoText.text)
            put("Time", System.currentTimeMillis())
        }
        Log.e("TAG", dataFile.path)
        dataFile.writeText(json.toString())
    }


    override fun onDestroyView() {
        saveData()

        super.onDestroyView()
    }
}