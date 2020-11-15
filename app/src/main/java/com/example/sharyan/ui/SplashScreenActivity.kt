package com.example.sharyan.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.sharyan.R
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        openApplicationOrShowSigningOptions()
        loginButton.setOnClickListener {
            goToLoginScreen()
        }
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
        translateLogoUpwards()
        showButtons()
    }

    /**
     * In order to show the "login" and "Register" buttons, the logo is translated
     * upwards; to free more space.
     */
    private fun translateLogoUpwards(){
        val translateAnim = AnimationUtils.loadAnimation(applicationContext, R.anim.anim_splash_screen)
        splashScreenLogo.startAnimation(translateAnim)
    }

    /**
     * The buttons are gradually shown to shown to the user by setting the alpha = 1
     */
    private fun showButtons(){
        loginButton.animate().alpha(1f).setDuration(1000).setStartDelay(500).start()
        registerButton.animate().alpha(1f).setDuration(1000).setStartDelay(500).start()
    }

    private fun goToLoginScreen(){
        startActivity(Intent(this, LoginActivity::class.java))
    }
}