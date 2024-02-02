package me.heid.heidtools.Toolbar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.provider.MediaStore
import android.view.ContextMenu
import android.view.View
import androidx.core.graphics.drawable.toDrawable
import com.google.android.gms.tasks.OnCompleteListener
import android.widget.Toast
import me.heid.heidtools.R
import me.heid.heidtools.profile.Profile
import java.io.FileNotFoundException

@SuppressLint("Registered")
open class BaseToolbarActivity: AppCompatActivity(){
    protected var mAuth: FirebaseAuth? = null
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance();
    }



    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menuInflater.inflate(R.menu.toolbar,menu)
        if(user != null) {
            var icon = user?.photoUrl
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, icon)
                .toDrawable(Resources.getSystem())
            if(bitmap.bitmap != null) {
                menu?.findItem(R.id.profile)?.setIcon(bitmap)
            }
        }


        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar,menu)
        if(user != null) {
            var icon = user?.photoUrl
            if(icon != null) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, icon)
                        .toDrawable(Resources.getSystem())
                    if (bitmap.bitmap != null) {
                        menu?.findItem(R.id.profile)?.setIcon(bitmap)
                    }
                }catch (e: FileNotFoundException){
                    Toast.makeText(this.baseContext,"Image not found",Toast.LENGTH_SHORT).show();
                    menu?.findItem(R.id.profile)?.setIcon(R.drawable.ic_supervisor_account_black_24dp)
                }
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.profile){
            var intent = Intent(this,Profile::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        user = mAuth?.currentUser

    }

    fun signUpUser(activity: Activity, email:String, password:String,updateUI : (user : FirebaseUser?) -> Unit){

        mAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener(activity,
            OnCompleteListener{
                task -> if(task.isSuccessful){
                    Log.d("user", "createUserWithEmail:success")
                    user = mAuth?.getCurrentUser()
                    updateUI(user)
                }else{
                    Log.w("user", "createUserWithEmail:Fail")
                    Log.d("createUserWithEmail", task.exception!!.toString())
                    if(task.exception!!.cause.toString().contains("com.google.firebase.auth.FirebaseAuthWeakPasswordException")){
                        Toast.makeText(activity,task.exception!!.message,Toast.LENGTH_LONG).show();
                    }
                }

        })
    }

    fun signInUser(activity: Activity, email:String, password:String,updateUI : (user : FirebaseUser?) -> Unit){
        mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener(this
        ) { task ->
    if (task.isSuccessful) {
        Log.d("USer", "signInWithEmail:success")
        val user = mAuth?.getCurrentUser()
        updateUI(user)
    } else {
        Log.w("USer", "signInWithEmail:failure", task.exception)
        Toast.makeText(this, "Authentication failed.",
            Toast.LENGTH_SHORT).show()
        updateUI(null)
    }
        }
    }
}