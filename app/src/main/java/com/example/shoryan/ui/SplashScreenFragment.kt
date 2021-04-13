package com.example.shoryan.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.shoryan.R
import com.example.shoryan.data.ServerError
import com.example.shoryan.databinding.FragmentSplashScreenBinding
import com.example.shoryan.di.MyApplication
import com.example.shoryan.repos.TokensRefresher
import com.example.shoryan.viewmodels.ProfileViewModel
import com.example.shoryan.viewmodels.SplashScreenViewModel
import com.example.shoryan.viewmodels.TokensViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashScreenFragment : Fragment() {

    @Inject
    lateinit var tokensViewModel: TokensViewModel
    @Inject
    lateinit var profileViewModel: ProfileViewModel
    private val splashScreenViewModel: SplashScreenViewModel by viewModels()
    private lateinit var navController: NavController
    // Used to display the animation only when the fragment is first created, otherwise skip animation
    private var firstTimeOpened = false

    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as MyApplication).appComponent.splashScreenComponent().create().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firstTimeOpened = true
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchWhenResumed {
            // Get the tokens from the dataStore
            splashScreenViewModel.retrieveTokensFromDataStore(requireContext())
            // To allow the user to see the logo
            delay(800)
        }.invokeOnCompletion {
            openApplicationOrShowSigningOptions()
        }
        binding.loginButton.setOnClickListener { goToLoginScreen() }
        binding.registerButton.setOnClickListener { goToRegistrationScreen() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instantiateNavController(view)
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun openApplicationOrShowSigningOptions() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        if(isUserLoggedIn() && isAccessTokenAlive()){
            openApplication()
        }
        else if(existsRefreshToken()){
            getNewAccessToken()
        }
        else{
            showSigningOptions()
        }
    }

    private fun isUserLoggedIn(): Boolean{
        return !TokensRefresher.accessToken.isNullOrEmpty()
    }

    /**
     * To check if the access token isn't expired, the access token is used to fetch the profile
     * of the user, if JWT_EXPIRED error is returned, the access token is therefore not alive
     * and the user should login again
     */
    private suspend fun isAccessTokenAlive(): Boolean{
        val response = profileViewModel.getProfileData()
        Log.e("RESPONSE_DEBUG", "////// ${response.toString()}")
        return response.error?.message != ServerError.JWT_EXPIRED
    }

    private fun existsRefreshToken() = !TokensRefresher.refreshToken.isNullOrEmpty()

    private fun getNewAccessToken() = lifecycleScope.launch{
        val response = tokensViewModel.getNewAccessToken(requireContext())
        // If new access token was generated
        if(response.accessToken != null){
            openApplication()
        }else{
            // else, generating new access token failed, show user signing options
            showSigningOptions()
        }
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
        binding.splashScreenLogo.startAnimation(translateAnim)
    }

    /**
     * The buttons are gradually shown to shown to the user by setting the alpha = 1
     */
    private fun showButtons(duration: Long, startDelay: Long){
        binding.loginButton.animate().alpha(1f).setDuration(duration).setStartDelay(startDelay).start()
        binding.registerButton.animate().alpha(1f).setDuration(duration).setStartDelay(startDelay).start()
    }

    private fun goToLoginScreen(){
        navController.navigate(R.id.action_splashScreenFragment_to_loginFragment)
    }

    private fun goToRegistrationScreen(){
        navController.navigate(R.id.action_splashScreenFragment_to_registrationFragment)
    }

}