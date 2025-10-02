package com.mycompany.parkingmanager.presentation.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.mycompany.parkingmanager.R
import com.mycompany.parkingmanager.domain.User
import com.mycompany.parkingmanager.databinding.ActivitySignUpBinding
import com.mycompany.parkingmanager.domain.utils.AuthenticationManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        binding.loginTV.setOnClickListener {
            finish()
        }

        binding.signUpButton.setOnClickListener {
            val email = binding.emailEdit.text.toString()
            val password = binding.passwordEdit.text.toString()
            val confirmPassword = binding.confirmPasswordEdit.text.toString()
            val user = User(email, password)

            if (!Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(), email)) {
                binding.emailEdit.error = "Invalid email"
                return@setOnClickListener
            }

            if (password.length < 6){
                binding.passwordEdit.error = "Length should be at least 6 characters"
                return@setOnClickListener
            }

            if (password != confirmPassword){
                binding.confirmPasswordEdit.error = "The passwords must match!"
                return@setOnClickListener
            }

            AuthenticationManager.signUp(this, user){
                if (it){
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}