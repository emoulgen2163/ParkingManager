package com.mycompany.parkingmanager.domain.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.mycompany.parkingmanager.R
import com.mycompany.parkingmanager.domain.User

object AuthenticationManager {
    private val auth = FirebaseAuth.getInstance()

    fun signUp(context: Context, user: User, signUpCallback: (Boolean) -> Unit){
        auth.createUserWithEmailAndPassword(user.email, user.password).addOnCompleteListener { task ->
            if (task.isSuccessful){
                signUpCallback(true)
            } else {
                signUpCallback(false)
                when(task.exception){
                    is FirebaseAuthUserCollisionException -> {
                        Toast.makeText(context, context.getString(R.string.user_exists_error), Toast.LENGTH_SHORT).show()
                    }
                    else -> Toast.makeText(context, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun login(context: Context, user: User, loginCallback: (Boolean) -> Unit){
        auth.signInWithEmailAndPassword(user.email, user.password).addOnCompleteListener { task ->
            if (task.isSuccessful){
                loginCallback(true)
            } else {
                loginCallback(false)
                when(task.exception){
                    is FirebaseAuthUserCollisionException -> Toast.makeText(context, context.getString(R.string.no_user_exists_error), Toast.LENGTH_SHORT).show()
                    is FirebaseAuthInvalidCredentialsException -> Toast.makeText(context, context.getString(R.string.invalid), Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(context, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun logout(){
        auth.signOut()
    }
}