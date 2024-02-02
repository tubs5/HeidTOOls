package me.heid.heidtools.stream.encode

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlin.math.pow

class CameraConfigDialog(configs: String, val function: (Configurations) -> Unit) : DialogFragment() {


    private val configurations = ArrayList<Configurations>()


      init {
          addConfigs(configs)
      }
    fun addConfigs(configs:String){
        val s = configs.split("[")
        s.forEach(action = {
            if(it.contains("min_duration:")){
                configurations.add(Configurations(it))
            }

        })
    }




    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{

        val builder = AlertDialog.Builder(
            requireContext()
        )

        val strings =  ArrayList<String>();
        configurations.forEach {
            strings.add(it.toString())
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, strings)



        builder.setTitle("Camera Selector").setAdapter(adapter,
            DialogInterface.OnClickListener { dialog, which ->
                for(c in configurations){
                    if(c.toString() == adapter.getItem(which)){
                        function.invoke(c)
                    }
                }



            })

        return builder.create()
    }

    public final class Configurations(s:String){
        val format: String
        val width : Int
        val height : Int
        val maxFps : Int

        init {
            val split = s.split(", ")
            width = split[0].substring(split[0].indexOf(":") + 1).toInt()
            height = split[1].substring(split[1].indexOf(":") + 1).toInt()
            format = split[2].substring(split[2].indexOf(":") + 1)
            maxFps = ((1 / (split[3].substring(split[3].indexOf(":") + 1)
                .toDouble())) * 10.0.pow(9)).toInt()
        }

        override fun toString(): String {
            return "$format \t\t ${width}x$height \t\t Fps:$maxFps"
        }



    }

}