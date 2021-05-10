package com.example.shoryan.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.shoryan.AndroidUtility
import com.example.shoryan.ConnectionLiveData
import com.example.shoryan.R
import com.example.shoryan.data.Reward
import com.example.shoryan.data.ServerError
import com.example.shoryan.di.MyApplication
import com.example.shoryan.interfaces.LoadingFragmentHolder
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.ui.composables.*
import com.example.shoryan.ui.theme.Gray
import com.example.shoryan.ui.theme.ShoryanTheme
import com.example.shoryan.viewmodels.RedeemingRewardsViewModel
import com.example.shoryan.viewmodels.RedeemingRewardsViewModelFactory
import com.example.shoryan.viewmodels.SMSViewModel
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class RedeemRewardFragment : Fragment(), LoadingFragmentHolder {
    @Inject
    lateinit var smsViewModel: SMSViewModel
    private val viewModel: RedeemingRewardsViewModel by viewModels {
        RedeemingRewardsViewModelFactory(
            requireActivity().application,
            RetrofitClient.getRetrofitClient().create(RetrofitBloodDonationInterface::class.java)
        )
    }
    private lateinit var connectionLiveData: ConnectionLiveData
    private val reward: Reward by lazy{
        requireArguments().get("reward") as Reward
    }
    private var chosenBranch: String? = null
    // Errors to show to user
    private val fragmentErrors = MutableStateFlow<String?>(null)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as MyApplication).appComponent.redeemRewardComponent().create().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
            connectionLiveData = ConnectionLiveData(requireContext())
        return ComposeView(requireContext()).apply {
            setContent {
                ShoryanTheme {
                    val connectionStatus = connectionLiveData.observeAsState(true).value
                    val loadingState = viewModel.rewardRedeemingState.collectAsState(RedeemingRewardsViewModel.RedeemingState.LOADING).value
                    Box(contentAlignment = Alignment.BottomCenter) {
                        when (loadingState) {
                            RedeemingRewardsViewModel.RedeemingState.LOADING -> LoadingScreen()
                            RedeemingRewardsViewModel.RedeemingState.LOADING_FAILED -> LoadingErrorScreen(
                                R.drawable.ic_refresh,
                                resources.getString(R.string.click_here_to_try_again),
                                ::fetchRewardDetails
                            )
                            else -> RewardScreen()
                        }
                        InternetConnectionBanner(requireContext(), Color.White, connectionStatus)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchRewardDetails()
        observeCodeVerificationStatus()
    }

    private fun fetchRewardDetails() = lifecycleScope.launchWhenResumed {
        viewModel.getRewardDetails(reward)
    }

    @Composable
    fun RewardScreen() {
        val scrollState = rememberScrollState()
        ConstraintLayout(
            Modifier.fillMaxSize().background(Color.White)
        ) {
            Column(
                Modifier
                    .verticalScroll(scrollState)
                    .padding(0.dp, 0.dp, 0.dp, 10.dp)
                    .fillMaxSize()
            ){
                val isBeingRedeemed: Boolean by viewModel.isBeingRedeemed.collectAsState(false)
                ConstraintLayout(Modifier.fillMaxSize()) {
                    val parentLayout = this
                    val ( appBar, points, cover, logo, description, branches, button) = createRefs()
                    CoverImage(parentLayout, cover)
                    AppBar(parentLayout, appBar)
                    if(!isBeingRedeemed) UserPoints(parentLayout, points, cover)
                    Logo(parentLayout, logo, cover, reward.imageLink)
                    OfferDescription(parentLayout, description, logo)
                    Branches(parentLayout, branches, logo, isBeingRedeemed)
                    RewardRedeemingStatus(parentLayout, branches, button, isBeingRedeemed)
                }
            }
        }

        ErrorsComposable()
    }

    @Composable
    fun ErrorsComposable(){
        Row(
            verticalAlignment  = Alignment.Bottom,
            modifier = Modifier
                .height(70.dp),
        ){
            val messageToUser: ServerError? by viewModel.messagesToUser.collectAsState(null)
            val fragmentErrorMessages: String? by fragmentErrors.collectAsState(null)
            messageToUser?.let {
                ShowSnackbar(resources.getString(it.errorStringResource)) {
                    OKSnackbarButton()
                }
            }
            fragmentErrorMessages?.let{
                ShowSnackbar(it){OKSnackbarButton()}
            }
        }
    }

    @Composable
    fun AppBar(
        parentLayout: ConstraintLayoutScope,
        appBar: ConstrainedLayoutReference
    ) {
        parentLayout.apply {
            AppBar(
                reward.rewardName,
                Modifier
                    .fillMaxWidth()
                    .constrainAs(appBar) {
                        top.linkTo(parent.top)
                    }
            )
        }
    }

    @Composable
    fun CoverImage(
        parentLayout: ConstraintLayoutScope,
        imageReference: ConstrainedLayoutReference
    ) {
        parentLayout.apply {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(imageReference) {
                        top.linkTo(parent.top, margin = 18.dp)
                    }
            ) {
                Image(
                    painterResource(R.mipmap.pharm_cover_image),
                    contentDescription = "Cover Image",
                    Modifier
                        .aspectRatio(1.8f)
                        .fillMaxWidth(),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }

    @Composable
    fun UserPoints(
        parentLayout: ConstraintLayoutScope,
        pointsReference: ConstrainedLayoutReference,
        coverReference: ConstrainedLayoutReference
    ) {
        parentLayout.apply {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomStart = 25.dp, topStart = 25.dp))
                    .background(Color.White)
                    .padding(25.dp, 15.dp, 15.dp, 10.dp)
                    .constrainAs(pointsReference) {
                        end.linkTo(coverReference.end)
                        top.linkTo(parent.top, margin = 70.dp)
                    }
            ) {
                Text(
                    text = resources.getString(R.string.point, viewModel.userPoints),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.Black,
                )
            }
        }
    }

    @Composable
    fun Logo(
        parentLayout: ConstraintLayoutScope,
        logoReference: ConstrainedLayoutReference,
        coverReference: ConstrainedLayoutReference,
        imageLink: String
    ) {
        parentLayout.apply {
            CoilImage(
                data = imageLink,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, Gray, CircleShape)
                    .size(110.dp)
                    .constrainAs(logoReference) {
                        top.linkTo(coverReference.bottom, margin = (-40).dp)
                        start.linkTo(parent.start, margin = 30.dp)
                    },
            )
        }
    }

        @Composable
        fun OfferDescription(
            parentLayout: ConstraintLayoutScope,
            descriptionReference: ConstrainedLayoutReference,
            logoReference: ConstrainedLayoutReference
        ) {
            parentLayout.apply{
                Text(
                    text = reward.description,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.subtitle2,
                    color = Color.Black,
                    modifier = Modifier.constrainAs(descriptionReference){
                        centerVerticallyTo(logoReference)
                        start.linkTo(logoReference.end, margin = 15.dp)
                    }
                )
            }
        }

    @Composable
    fun Branches(
        parentLayout: ConstraintLayoutScope,
        branchesReference: ConstrainedLayoutReference,
        logoReference: ConstrainedLayoutReference,
        isBeingRedeemed: Boolean
    ){

        parentLayout.apply{
            Column(
                modifier = Modifier
                    .constrainAs(branchesReference) {
                        start.linkTo(logoReference.start)
                        end.linkTo(parent.end)
                        top.linkTo(logoReference.bottom, 40.dp)
                    }
                    .fillMaxWidth()
            ){
                Text(
                    text = resources.getString(R.string.branches),
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.Black,
                )

                val isOpen = remember { mutableStateOf(false) }
                val openCloseOfDropDownList: (Boolean) -> Unit = {
                    isOpen.value = it
                }
                val selectedString: (String) -> Unit = {
                    chosenBranch = it
                }

                Box{
                    Column{
                        OutlinedTextField(
                            value = chosenBranch ?: "---",
                            onValueChange = { chosenBranch = it },
                            readOnly = true,
                            label = { Text(text = resources.getString(R.string.choose_branch)) },
                            modifier = Modifier.fillMaxWidth(0.95f)
                        )
                        DropDownComposable(
                            isOpen.value,
                            reward.branches,
                            openCloseOfDropDownList,
                            selectedString
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (!isBeingRedeemed) isOpen.value = true
                                }
                            )
                    )
                }
            }
        }
    }

    @Composable
    fun RewardRedeemingStatus(
        parentLayout: ConstraintLayoutScope,
        branches: ConstrainedLayoutReference,
        button: ConstrainedLayoutReference,
        isBeingRedeemed: Boolean
    ){
        if(isBeingRedeemed){
            CodeEntryUI(parentLayout, button, branches)
        }
        else{
            if(canUserRedeemReward())
                RedeemButton(parentLayout, button, branches)
            else
                InsufficientPointsButton(parentLayout, button, branches)
        }
    }

    @Composable
    fun CodeEntryUI(parentLayout: ConstraintLayoutScope, button: ConstrainedLayoutReference, branchesReference: ConstrainedLayoutReference) {
        val canResendSMS = viewModel.canResendSMS
        val remainingTime =  viewModel.remainingTimeString
        parentLayout.apply {
            CodeEntryUI(
                numberOfCells = 6,
                onCodeEntered = ::verifyCode,
                screenWidth = AndroidUtility.getScreenWidth(requireContext()),
                screenHeight = AndroidUtility.getScreenHeight(requireContext()),
                buttonText = resources.getString(R.string.confirm),
                resendCodeText = resources.getString(R.string.resend_sms),
                onResendCodeClicked = ::sendSMS,
                canResendSMS = canResendSMS,
                remainingTime = remainingTime,
                locale = requireContext().resources.configuration.locale,
                modifier = Modifier.constrainAs(button){
                    top.linkTo(branchesReference.bottom, 20.dp)
                    centerHorizontallyTo(parent)
                }.fillMaxSize()
            )
        }
    }

    @Composable
    fun RedeemButton(
        parentLayout: ConstraintLayoutScope,
        buttonReference: ConstrainedLayoutReference,
        branchesReference: ConstrainedLayoutReference,
    ) {

        RewardButton(
            parentLayout,
            buttonReference,
            branchesReference,
            true,
            resources.getString(R.string.redeem),
            ::onRedeemButtonClicked
        )

    }

    @Composable
    fun InsufficientPointsButton(
        parentLayout: ConstraintLayoutScope,
        buttonReference: ConstrainedLayoutReference,
        branchesReference: ConstrainedLayoutReference
    ) {
        RewardButton(
            parentLayout,
            buttonReference,
            branchesReference,
            false,
            resources.getString(R.string.points_remaining, reward.points - viewModel.userPoints)
        ) {}
    }

    @Composable
    fun RewardButton(
        parentLayout: ConstraintLayoutScope,
        buttonReference: ConstrainedLayoutReference,
        branchesReference: ConstrainedLayoutReference,
        enabled: Boolean,
        text: String,
        onClick: () -> Unit
    ){
        parentLayout.apply{
            Button(
                onClick = onClick,
                enabled = enabled,
                contentPadding = PaddingValues(20.dp),
                shape = RoundedCornerShape(29.dp),
                modifier = Modifier
                    .width(350.dp)
                    .constrainAs(buttonReference) {
                        top.linkTo(branchesReference.bottom, 20.dp)
                        centerHorizontallyTo(parent)
                    }
            ){
                Text(
                    text = text,
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.White
                )
            }
        }
    }



    private fun onRedeemButtonClicked(){
        if(chosenBranch != null)
            showAlertDialog(reward.id)
        else{
            fragmentErrors.value = resources.getString(R.string.no_branch_chosen)
        }
    }

    private fun redeemReward(rewardId: String) {
        lifecycleScope.launchWhenResumed {
            viewModel.tryRedeemReward(rewardId)
        }
    }

    @Composable
    fun ShowSnackbar(message: String, button: @Composable () -> Unit){
        Snackbar(
          action = button,
        ) {
            Text(message)
        }
    }

    @Composable
    fun TryAgainSnackbarButton(){
        Button(
            onClick = {
                redeemReward(reward.id)
                viewModel.clearReceivedMessage()
            }
        ){
            Text(resources.getString(R.string.try_again))
        }
    }

    @Composable
    fun OKSnackbarButton(){
        Button(
            onClick = {
                fragmentErrors.value = null
                viewModel.clearReceivedMessage()
            }
        ){
            Text(resources.getString(R.string.ok))
        }
    }

    private fun canUserRedeemReward() = viewModel.userPoints >= reward.points

    private fun showAlertDialog(id: String) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        with(builder) {
            setTitle(resources.getString(R.string.redeeming_confirmation_title))
            setMessage(resources.getString(R.string.redeeming_confirmation_body))
            setPositiveButton(resources.getString(R.string.confirm)) { _, _ -> redeemReward(id) }
            setNegativeButton(resources.getString(R.string.no),null)
            show()
        }
    }

    private fun verifyCode(code: String){
        //Toast.makeText(requireContext(), code, Toast.LENGTH_LONG).show()
        viewModel.verifyCode(code)
    }

    private fun sendSMS(){
        viewModel.trySendSMS(reward.id)
    }

    private fun observeCodeVerificationStatus(){
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.isVerifyingCode.collect{
                if(it){
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

    override fun onLoadingFragmentDismissed() {
        viewModel.stopVerifying()
    }
}
