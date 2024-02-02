package me.heid.heidtools.work

import android.animation.Animator
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import kotlinx.android.synthetic.main.fragment_work_view.*
import me.heid.heidtools.BuildConfig
import me.heid.heidtools.R
import org.json.JSONObject
import java.io.File

class WorkView : Fragment() {
    lateinit var providerUri:Uri;
    lateinit var extproviderUri:Uri;

    private var currentAnimator: Animator? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
        val path = arguments?.getString("path")

        providerUri = FileProvider.getUriForFile(this.requireContext(), BuildConfig.APPLICATION_ID +".provider",File(path))
        val file = File(requireContext().externalCacheDir,"exp")
        extproviderUri = FileProvider.getUriForFile(this.requireContext(), BuildConfig.APPLICATION_ID +".provider",file)


        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            parentFragmentManager.beginTransaction().setReorderingAllowed(true).replace(R.id.fragment_container_view,WorkBrowse::class.java,arguments).commit()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_work_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val path = arguments?.getString("path")



        val basecache = File(requireContext().externalCacheDir,"exp")
        val nr = basecache.listFiles()?.size?: 0
        if(File(path+"/Key.jpeg").exists()){
            val keyImage = BitmapFactory.decodeFile(path+"/Key.jpeg")

            WorkViewKeyImage.rotation = 90f
            WorkViewKeyImage.setImageBitmap(keyImage)
            WorkViewKeyImage.setOnLongClickListener{
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    type = "image/*"
                    flags =  Intent.FLAG_GRANT_READ_URI_PERMISSION
                    setDataAndType(extproviderUri.buildUpon().appendPath("$nr/Key.jpeg").build(),"image/jpeg")
                }
                startActivity(intent);
                return@setOnLongClickListener true
            }
            WorkViewKeyImage.setOnClickListener{
                val b = Bundle().apply {
                    putString("uri",path+"/Key.jpeg")
                }
                parentFragmentManager.beginTransaction().setReorderingAllowed(true).addToBackStack(null).add(R.id.fragment_container_view,WorkViewImageFragment::class.java,b).commit()

                //scaleUpImage((path+"/Key.jpeg").toUri(),WorkViewImageViewFullScreen)
            }


        }
        if(File(path+"/Dash.jpeg").exists()){
            val dashImage = BitmapFactory.decodeFile(path+"/Dash.jpeg")
            WorkViewDashImage.setImageBitmap(dashImage)
            WorkViewDashImage.rotation = 90f
            WorkViewDashImage.setOnLongClickListener{
                val intent : Intent = Intent(Intent.ACTION_VIEW).apply {
                    type = "image/*"
                    flags =  Intent.FLAG_GRANT_READ_URI_PERMISSION
                    setDataAndType(extproviderUri.buildUpon().appendPath("$nr/Dash.jpeg").build(),"image/jpeg")
                    // putExtra(Intent.EXTRA_STREAM,extproviderUri.buildUpon().appendPath("$nr/Key.jpeg").build())
                }
                startActivity(intent)
                return@setOnLongClickListener true
            }
            WorkViewDashImage.setOnClickListener{
                val b = Bundle().apply {
                    putString("uri",path+"/Dash.jpeg")
                }
                parentFragmentManager.beginTransaction().setReorderingAllowed(true).addToBackStack(null).add(R.id.fragment_container_view,WorkViewImageFragment::class.java,b).commit()

            }
        }
        val extras = File(path,"Extra")

        for (file in extras.listFiles()){
            val view2 = ImageView(this.requireContext())
            val img = BitmapFactory.decodeFile(file.path)
           // view2.layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT)
            view2.setImageBitmap(img)
            view2.rotation = 90f
            view2.setOnLongClickListener{
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    flags =  Intent.FLAG_GRANT_READ_URI_PERMISSION
                    setDataAndType(extproviderUri.buildUpon().appendPath("$nr/Extra/${file.name}").build(),"image/jpeg")
                }
                startActivity(intent)
                return@setOnLongClickListener true
            }
            view2.setOnClickListener{
                val b = Bundle().apply {
                    putString("uri",file.path)
                }
                parentFragmentManager.beginTransaction().setReorderingAllowed(true).addToBackStack(null).add(R.id.fragment_container_view,WorkViewImageFragment::class.java,b).commit()

            }

            WorkViewScrollExtraLayout.addView(view2,LinearLayout.LayoutParams(300,300))
        }



        if(File(path+"/Key.jpeg").exists()){
            File(path!!,"Key.jpeg").copyTo(File(basecache,"$nr/Key.jpeg"))
        }
        if(File(path+"/Dash.jpeg").exists()){
            File(path!!,"Dash.jpeg").copyTo(File(basecache,"$nr/Dash.jpeg"))
        }
        for (file in extras.listFiles()){
            file.copyTo(File(basecache,"$nr/Extra/${file.name}"))
        }
        if(File(path+"/data.json").exists()){
            val text = File(path+"/data.json").readText()
            if(text.isNotEmpty()){
                val json = JSONObject(text)
                WorkViewExtraText.text = if(json.has("Text")){
                    json.getString("Text")
                }else ""
                workViewKm.text = if(json.has("Km")){
                    json.getString("Km")
                }else ""
                workViewSerieNr.text = if(json.has("Serial")){
                    json.getString("Serial")
                }else ""
            }
            Log.e("TAG",File("$path/data.json").path)
        }




        WorkViewDashImage.invalidate()
        WorkViewKeyImage.invalidate()
        WorkViewScrollExtra.invalidate()
        WorkViewScrollExtraLayout.invalidate()
        WorkViewExtraText.invalidate()




        WorkViewButton.setOnClickListener(View.OnClickListener {

            //val furi = providerUri.buildUpon().appendPath("Key.jpeg").build()
            val pa = extproviderUri.buildUpon().appendPath("$nr/Key.jpeg").build()


            val imageUris = ArrayList<Uri>().apply {
                if(File(path+"/Key.jpeg").exists()){
                    add(extproviderUri.buildUpon().appendPath("$nr/Key.jpeg").build())
                }
                if(File(path+"/Dash.jpeg").exists()){
                    add(extproviderUri.buildUpon().appendPath("$nr/Dash.jpeg").build())
                }

                for (file in extras.listFiles()){
                    add(extproviderUri.buildUpon().appendPath("$nr/Extra/${file.name}").build())
                }

            }








            //val furi = File(basecache,"$nr/Key.jpeg").toUri()
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(requireContext().getSharedPreferences("Work",0).getString("SubjectEmail","").toString()))
                putExtra(Intent.EXTRA_SUBJECT,File(path).name +"\t"+ workViewKm.text.toString() +"\t"+ workViewSerieNr.text.toString())
                putExtra(Intent.EXTRA_TEXT,WorkViewExtraText.text.toString() +"\t"+ workViewKm.text.toString() +"\t"+ workViewSerieNr.text.toString())
                flags = (Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putParcelableArrayListExtra(Intent.EXTRA_STREAM,imageUris)
                type = "image/*"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            shareIntent.flags = (Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)


            startActivity(shareIntent)

        })

        WorkViewButton2.setOnClickListener{
            val builder: AlertDialog.Builder = activity.let {
                AlertDialog.Builder(it)

            }
            builder.setPositiveButton("Yes") { dialog, which ->
                if (path != null) {
                    File(path).deleteRecursively()
                    requireActivity().onBackPressed()


                }
            }
            builder.setNegativeButton("No", DialogInterface.OnClickListener{
                    dialog, which ->
            })
            builder.setMessage("Are you sure?")
                ?.setTitle("Delete This Entry!")

            val dialog: AlertDialog = builder.create()
            dialog.show()

        }

        WorkViewButton3.setOnClickListener{
            val b = Bundle()
            b.putString("path",path)
            parentFragmentManager.beginTransaction().setReorderingAllowed(true).replace(R.id.fragment_container_view,WorkAddCamera2::class.java,b).commit()
        }




    }



}