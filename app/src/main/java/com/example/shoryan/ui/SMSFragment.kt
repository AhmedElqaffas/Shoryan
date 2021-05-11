package com.example.shoryan.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import com.example.shoryan.AndroidUtility.Companion.getScreenHeight
import com.example.shoryan.AndroidUtility.Companion.getScreenWidth
import com.example.shoryan.R
import com.example.shoryan.data.RegistrationQuery
import com.example.shoryan.data.Tokens
import com.example.shoryan.di.MyApplication
import com.example.shoryan.interfaces.LoadingFragmentHolder
import com.example.shoryan.ui.composables.PinEntryComposable
import com.example.shoryan.ui.theme.ShoryanTheme
import com.example.shoryan.viewmodels.SMSViewModel
import com.example.shoryan.viewmodels.SMSViewModel.OperationType.LOGIN
import com.example.shoryan.viewmodels.SMSViewModel.OperationType.REGISTRATION
import com.example.shoryan.viewmodels.TokensViewModel
import kotlinx.coroutines.flow.collect
import java.util.*
import javax.inject.Inject

class SMSFragment : Fragment(), LoadingFragmentHolder {

    val viewModel: SMSViewModel by navGraphViewModels(R.id.landing_nav_graph)
    @Inject
    lateinit var tokensViewModel: TokensViewModel
    private lateinit var navController: NavController
    private val phoneNumber: String by lazy{
        requireArguments().get("phoneNumber") as String
    }
    // In case of registration, this will contain the user's details, otherwise, it is null
    private val registrationQuery: RegistrationQuery? by lazy{
        requireArguments().get("registrationQuery") as RegistrationQuery?
    }
    // Represents the code entered in the PinEntryComposable
    private var enteredCode: String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as MyApplication).appComponent.smsComponent().create().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeNavController(view)
        viewModel.trySendSMS(phoneNumber, registrationQuery)
        observeLoggingStatus()
        observeCodeVerificationStatus()
    }

    private fun initializeNavController(view: View) {
        navController = Navigation.findNavController(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        return ComposeView(requireContext()).apply {
            setContent{
                ShoryanTheme{
                    smsScreen()
                }
            }
        }
    }

    @Composable
    fun smsScreen(){
        Column(
            Modifier.fillMaxSize()
        ) {
            // Banner and back button
            Surface(){
                Banner()
                BackButton()
            }
            Title()
            Statement()
            CodeField()
            ConfirmButton()
            ResendCodeText()
            ErrorSnackBar()
        }
    }

    @Composable
    fun Banner(){
        Image(
            painterResource(R.drawable.login_banner),
            contentDescription = "Login Banner",
            Modifier
                .aspectRatio(2.75f)
                .fillMaxWidth(),
            contentScale = ContentScale.FillBounds
        )
    }
    
    @Composable
    fun BackButton(){
        // To avoid the button being drawn on the right of the screen in case of arabic locale
        val arrangement = if(requireContext().resources.configuration.locale == Locale.ENGLISH){
            Arrangement.Start
        }else{
            Arrangement.End
        }
        Row(
            horizontalArrangement = arrangement,
            modifier = Modifier
                .padding(20.dp)
                .clickable(onClick = { navController.popBackStack() })
                .fillMaxWidth()
        ) {
            Image(
                painterResource(R.mipmap.ic_back_oval),
                contentDescription = "Back Button"
            )
        }
    }


    @Composable
    fun Title(){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = resources.getString(R.string.confirmation_code),
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(0.dp, getScreenHeight(requireContext())*0.05f, 0.dp, 0.dp)
            )
        }
    }

    @Composable
    fun Statement(){
        Text(
            text = resources.getString(R.string.enter_code,phoneNumber),
            modifier = Modifier.padding(20.dp, getScreenHeight(requireContext())*0.07f, 0.dp, 0.dp),
            color = MaterialTheme.colors.primaryVariant,
            style = MaterialTheme.typography.body1
        )
    }

    @Composable
    fun CodeField(){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, getScreenHeight(requireContext()) * 0.07f, 0.dp, 0.dp),
            horizontalArrangement = Arrangement.Center
        ){
            PinEntryComposable(
                numberOfCells = 4,
                modifier = Modifier.fillMaxWidth(0.78f),
                onChange = ::updateCodeInstance,
                onCodeEntered = ::verifyCode,
                locale = requireContext().resources.configuration.locale
            )
        }
    }

    @Composable
    fun ConfirmButton() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, getScreenHeight(requireContext()) * 0.09f, 0.dp, 0.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { verifyCode(enteredCode) },
                enabled = true,
                contentPadding = PaddingValues(20.dp),
                shape = RoundedCornerShape(29.dp),
                modifier = Modifier
                    .width(getScreenWidth(requireContext())*0.8f)
            ){
                Text(
                    text = resources.getString(R.string.confirm),
                    style = MaterialTheme.typography.body1,
                    color = Color.White
                )
            }
        }
    }

    @Composable
    fun ResendCodeText(){
        val canResendSMS: Boolean by viewModel.canResendSMS.collectAsState(true)
        val remainingTime: String by viewModel.remainingTimeString.collectAsState("")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, getScreenHeight(requireContext()) * 0.04f, 0.dp, 0.dp),
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = if(canResendSMS) resources.getString(R.string.resend_sms) else remainingTime,
                color = MaterialTheme.colors.secondaryVariant,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.clickable(
                    onClick = {
                        if(canResendSMS) sendSMS()
                    }
                )
            )
        }
    }

    @Composable
    fun ErrorSnackBar(){
        val error = viewModel.eventsFlow.collectAsState(null)
        error.value?.let{
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 0.dp, 0.dp, 20.dp)
                    .requiredHeight(70.dp),
                horizontalArrangement = Arrangement.Center
            ){
                Snackbar(
                    modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp),
                    shape = RoundedCornerShape(8.dp),
                    action = {
                        Button(
                            shape = RoundedCornerShape(10.dp),
                            onClick = {viewModel.clearReceivedEvent()},
                        ){
                            Text(resources.getString(R.string.ok))
                        }
                    }
                ) {
                    Text(resources.getString(error.value!!.errorStringResource))
                }
            }
        }
    }

    private fun sendSMS(){
        // If "registrationQuery" isn't null, then this fragment is for registration, otherwise it is
        // for login
        viewModel.trySendSMS(phoneNumber, registrationQuery)
    }

    private fun updateCodeInstance(code: String){
        enteredCode = code
    }

    private fun verifyCode(code: String){
        // If "registrationQuery" isn't null, then this fragment is for registration, otherwise it is
        // for login
        val operation = if(registrationQuery != null) REGISTRATION else LOGIN
        viewModel.verifyCode(phoneNumber, code, operation)
    }

    private fun observeCodeVerificationStatus(){
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.isVerifyingCode.collect{
                if(it.name == "VERIFYING"){
                    showProcessingIndicator()
                }
                else{
                    hideProcessingIndicator()
                }
            }
        }
    }

    private fun showProcessingIndicator(){
        val loadingFragment: DialogFragment? = childFragmentManager.findFragmentByTag("loading") as DialogFragment?
        if(loadingFragment == null)
            LoadingFragment(this).show(childFragmentManager, "loading")
    }

    private fun hideProcessingIndicator(){
        val loadingFragment: DialogFragment? = childFragmentManager.findFragmentByTag("loading") as DialogFragment?
        loadingFragment?.dismiss()
    }

    private fun observeLoggingStatus(){
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.loggingCredentials.collect{
                if(it != null){
                    saveTokens(it)
                    hideProcessingIndicator()
                    openHomeScreen()
                }
            }
        }
    }

    private suspend fun saveTokens(tokens: Tokens){
        tokensViewModel.saveTokens(tokens, requireContext())
    }

    private fun openHomeScreen(){
        val intent = Intent(activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onLoadingFragmentDismissed() {
        viewModel.stopVerifying()
    }
}