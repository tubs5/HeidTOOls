package me.heid.heidtools.profile

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_profile.*
import me.heid.heidtools.Toolbar.BaseToolbarActivity
import me.heid.heidtools.R

class Profile : BaseToolbarActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


    }

    override fun onStart() {
        super.onStart()

        if(user == null){
            var intent = Intent(this,LoginActivity::class.java)
            intent.flags = FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityForResult(intent,986)
        }

        button3.setOnClickListener(View.OnClickListener {
            mAuth?.signOut();
            finish()
        })

    }
}
