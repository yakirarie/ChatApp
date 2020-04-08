package com.yakirarie.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.yakirarie.chatapp.R
import com.yakirarie.chatapp.dialogs.LoadingDialog
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val loadingDialog = LoadingDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.title = "Login"
    }

    fun loginLoginBtnClicked(view: View) {
        val email = loginEmailText.text.toString()
        val password = loginPasswordText.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all of the above", Toast.LENGTH_SHORT).show()
            return
        }
        loadingDialog.show(supportFragmentManager, "loading")
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                loadingDialog.dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to login: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
                loadingDialog.dismiss()
            }

    }


    fun loginCreateUserBtnClicked(view: View) {
        val createUserIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserIntent)

    }
}
