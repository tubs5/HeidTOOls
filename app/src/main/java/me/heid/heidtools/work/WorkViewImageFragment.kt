package me.heid.heidtools.work

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.fragment_work_view_image.*
import me.heid.heidtools.R

class WorkViewImageFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_work_view_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         val uri = requireArguments().getString("uri","").toUri()
         workViewImageImage.setImageURI(uri)
         workViewImageImage.setOnClickListener{
             parentFragmentManager.popBackStack()
         }

    }

}