package com.example.shoryan.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.shoryan.LocaleHelper
import com.example.shoryan.NotificationDestinationManager
import com.example.shoryan.R
import com.example.shoryan.data.NotificationData
import com.example.shoryan.data.ServerError
import com.example.shoryan.databinding.FragmentSplashScreenBinding
import com.example.shoryan.interfaces.LocaleChangerHolder
import com.example.shoryan.repos.TokensRefresher
import com.example.shoryan.viewmodels.ProfileViewModel
import com.example.shoryan.viewmodels.SplashScreenViewModel
import com.example.shoryan.viewmodels.TokensViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_splash_screen.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SplashScreenFragment : Fragment(), LocaleChangerHolder {

    val tokensViewModel: TokensViewModel by viewModels()
    val profileViewModel: ProfileViewModel by viewModels()
    private val splashScreenViewModel: SplashScreenViewModel by viewModels()
    private lateinit var navController: NavController

    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        binding.viewModel = splashScreenViewModel
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreenViewModel.updateLocale(LocaleHelper.getLanguage(requireContext()))
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchWhenResumed {
            // Get the tokens from the dataStore
            splashScreenViewModel.retrieveTokensFromDataStore(requireContext())
            if(splashScreenViewModel.firstTimeOpened){
                // To allow the user to see the logo
                delay(800)
            }
        }.invokeOnCompletion {
            openApplicationOrShowSigningOptions()
        }
        binding.loginButton.setOnClickListener { goToLoginScreen() }
        binding.registerButton.setOnClickListener { goToRegistrationScreen() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instantiateNavController(view)
        localeButton.setOnClickListener { openLanguageFragment() }
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun openApplicationOrShowSigningOptions() = lifecycleScope.launchWhenResumed {
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

        if(isDirectedHereByNotification()){
            goToDestinedFragment()
        }/*else{
            // The user opened the app normally
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                if (!TextUtils.isEmpty(token)) {
                    startActivity(Intent(activity, MainActivity::class.java))
                    println("///////// $token")
                    activity?.finish()
                } else{
                    Log.w("lANDING", "token should not be null...");
                }
            }
        }*/else{
            // The user opened the app normally
            startActivity(Intent(activity, MainActivity::class.java))
            activity?.finish()
        }
    }

    private fun isDirectedHereByNotification() = requireActivity().intent.extras?.get("notificationId")  != null

    private fun goToDestinedFragment(){
        val notificationData = requireActivity().intent.extras?.get("notificationData") as NotificationData
        val navDeepLink = NotificationDestinationManager.createNavDeepLink(requireContext(), notificationData.data)
        val pendingIntent = navDeepLink.createPendingIntent()
        pendingIntent.send()
        activity?.finish()
    }

    private fun showSigningOptions() {
        if(splashScreenViewModel.firstTimeOpened){
            translateLogoUpwards(800)
            showButtons(1000,500)
            splashScreenViewModel.firstTimeOpened = false
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
        binding.localeButton.visibility = View.VISIBLE
        binding.registerButton.visibility = View.VISIBLE
        binding.loginButton.visibility = View.VISIBLE
        binding.localeButton.animate().alpha(1f).setDuration(duration).setStartDelay(startDelay).start()
        binding.loginButton.animate().alpha(1f).setDuration(duration).setStartDelay(startDelay).start()
        binding.registerButton.animate().alpha(1f).setDuration(duration).setStartDelay(startDelay).start()
        binding.banner.animate().alpha(1f).setDuration(duration).setStartDelay(startDelay).start()
    }

    private fun goToLoginScreen(){
        navController.navigate(R.id.action_splashScreenFragment_to_loginFragment)
    }

    private fun goToRegistrationScreen(){
        navController.navigate(R.id.action_splashScreenFragment_to_registrationFragment)
    }

    private fun openLanguageFragment(){
        LanguageFragment().show(childFragmentManager, "language")
    }

    override fun onLocaleChanged(newLanguageTag: String){
        updateLocale(newLanguageTag)
        requireActivity().recreate()
    }

    private fun updateLocale(newLanguageTag: String) {
        LocaleHelper.persist(requireContext(), newLanguageTag)
    }
}