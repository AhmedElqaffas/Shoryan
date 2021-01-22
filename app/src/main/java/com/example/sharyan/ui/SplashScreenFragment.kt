package com.example.sharyan.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sharyan.R
import kotlinx.android.synthetic.main.fragment_splash_screen.*


class SplashScreenFragment : Fragment() {

    private lateinit var navController: NavController
    // Used to display the animation only when the fragment is first created, otherwise skip animation
    private var firstTimeOpened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firstTimeOpened = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instantiateNavController(view)
        openApplicationOrShowSigningOptions()
        loginButton.setOnClickListener { goToLoginScreen() }
        registerButton.setOnClickListener { goToRegistrationScreen() }
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun openApplicationOrShowSigningOptions() {
        if(isUserLoggedIn()){
            openApplication()
        }
        else{
            showSigningOptions()
        }
    }

    private fun isUserLoggedIn(): Boolean{
        return false
    }

    private fun openApplication(){
        startActivity(Intent(activity, MainActivity::class.java))
        activity?.finish()
    }

    private fun showSigningOptions() {
        if(firstTimeOpened){
            translateLogoUpwards(800)
            showButtons(1000,500)
            firstTimeOpened = false
        }
        else{
            translateLogoUpwards(0)
            showButtons(0, 0)
        }
    }

    /**
     * In order to show the "login" and "Register" buttons, the logo is translated
     * upwards; to free more space.
     */
    private fun translateLogoUpwards(duration: Long){
        val translateAnim = AnimationUtils.loadAnimation(activity, R.anim.anim_splash_screen)
        translateAnim.duration = duration
        splashScreenLogo.startAnimation(translateAnim)
    }

    /**
     * The buttons are gradually shown to shown to the user by setting the alpha = 1
     */
    private fun showButtons(duration: Long, startDelay: Long){
        loginButton.animate().alpha(1f).setDuration(duration).setStartDelay(startDelay).start()
        registerButton.animate().alpha(1f).setDuration(duration).setStartDelay(startDelay).start()
    }

    private fun goToLoginScreen(){
        navController.navigate(R.id.action_splashScreenFragment_to_loginFragment)
    }

    private fun goToRegistrationScreen(){
        navController.navigate(R.id.action_splashScreenFragment_to_registrationFragment)
    }

}