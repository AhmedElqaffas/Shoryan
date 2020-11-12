package com.example.sharyan.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.sharyan.R
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
    }

    override fun onResume() {
        super.onResume()
        openApplicationOrShowSigningOptions()
    }

    private fun openApplicationOrShowSigningOptions(){
        if(isUserLoggedIn()){
            openApplication()
        }
        else{
            showSigningOptions()
        }
    }

    private fun openApplication(){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun isUserLoggedIn(): Boolean{
        return false
    }

    private fun showSigningOptions(){
        changeLogoLocationAndSize()
        showButtons()
    }

    /**
     * In order to show the "login" and "Register" buttons, the logo is translated
     * upwards; to free more space.
     */
    private fun changeLogoLocationAndSize(){
        translateView(splashScreenLogo,R.anim.splash_screen)
    }

    private fun showButtons(){
        loginButton.visibility = View.VISIBLE
        registerButton.visibility = View.VISIBLE
        translateButtonsWithLogo()
    }

    private fun translateButtonsWithLogo(){
        translateView(loginButton,R.anim.signing_buttons)
        translateView(registerButton,R.anim.signing_buttons)
    }

    private fun translateView(view: View, animationResource: Int){
        val translateAnim = AnimationUtils.loadAnimation(applicationContext, animationResource)
        view.startAnimation(translateAnim)
    }
}