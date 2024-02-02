package me.heid.heidtools.work

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.activity.addCallback
import androidx.transition.TransitionInflater
import kotlinx.android.synthetic.main.fragment_work_calendar_picker.*
import kotlinx.android.synthetic.main.fragment_work_settings.*
import me.heid.heidtools.R

class WorkSettings : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            parentFragmentManager.beginTransaction().setReorderingAllowed(true).replace(R.id.fragment_container_view,WorkBrowse::class.java,null).commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_work_settings, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        workSettingsEmailEditText.text.append(
            requireContext().getSharedPreferences("Work",0).getString("SubjectEmail",""))

        workSettingsEmailEditText.setOnFocusChangeListener(
            View.OnFocusChangeListener{
                v, hasFocus ->
                requireContext().getSharedPreferences("Work",0).edit().putString("SubjectEmail",workSettingsEmailEditText.text.toString()).apply()
            }

        )


    }

}