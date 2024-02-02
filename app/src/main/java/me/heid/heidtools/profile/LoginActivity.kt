package me.heid.heidtools.profile

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import me.heid.heidtools.Toolbar.BaseToolbarActivity
import me.heid.heidtools.R

class LoginActivity : BaseToolbarActivity(),AdapterView.OnItemSelectedListener {


    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        val ar = resources.getStringArray(R.array.LoginChoice)

        when(parent.getItemAtPosition(pos).toString()){
            //Email
            ar[0] ->
                setupEmail()
            //Phone
            ar[1] ->
                setupPhone()
            //Google
            ar[2] ->
                setupGoogle()


        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }


    var gso:GoogleSignInOptions? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        spinner.onItemSelectedListener = this

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }




    fun setupEmail(){
        username.visibility = View.VISIBLE
        password.visibility = View.VISIBLE
        username.hint = "Email"
        username.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        login.setOnClickListener(View.OnClickListener {
            signUpUser(this,username.text.toString(),password.text.toString(),::updateUI)
        })


        (password.parent as ViewGroup).invalidate()
    }
    fun setupPhone(){
        username.visibility = View.VISIBLE
        password.visibility = View.GONE
        username.hint = "Phone number"
        username.inputType = InputType.TYPE_CLASS_PHONE

        (password.parent as ViewGroup).invalidate()
    }
    fun setupGoogle(){
        username.visibility  = View.GONE
        password.visibility = View.GONE
        (password.parent as ViewGroup).invalidate()




        login.setOnClickListener(View.OnClickListener {
            val googleSignInClient = GoogleSignIn.getClient(this, gso!!)
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        })
    }


    var RC_SIGN_IN = 456
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("User", "Google sign in failed", e)
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("USER", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("USER", "signInWithCredential:success")
                    val user = mAuth?.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("USER", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this,"AUTH FAILED",Toast.LENGTH_LONG).show()
                    updateUI(null)
                }
            }
    }

    fun updateUI(user:FirebaseUser?){
        finish()
    }

}
