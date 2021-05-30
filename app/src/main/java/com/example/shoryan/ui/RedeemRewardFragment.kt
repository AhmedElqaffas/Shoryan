package com.example.shoryan.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.shoryan.AndroidUtility
import com.example.shoryan.ConnectionLiveData
import com.example.shoryan.R
import com.example.shoryan.data.Branch
import com.example.shoryan.data.ServerError
import com.example.shoryan.interfaces.LoadingFragmentHolder
import com.example.shoryan.ui.composables.*
import com.example.shoryan.ui.theme.Gray
import com.example.shoryan.ui.theme.ShoryanTheme
import com.example.shoryan.viewmodels.RedeemingRewardsViewModel
import com.example.shoryan.viewmodels.SMSViewModel
import com.example.shoryan.viewmodels.TokensViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import java.util.*

@AndroidEntryPoint
class RedeemRewardFragment : Fragment(), LoadingFragmentHolder {
    private val smsViewModel: SMSViewModel by viewModels()
    private lateinit var navController: NavController
    private val viewModel: RedeemingRewardsViewModel by viewModels()
    private val tokensViewModel: TokensViewModel by viewModels()
    private lateinit var connectionLiveData: ConnectionLiveData
    private val rewardId: String by lazy {
        requireArguments().get("rewardId") as String
    }
    private var chosenBranchId: String? = null
    private var chosenBranchAddress: String? = null

    // Errors to show to user
    private val fragmentErrors = MutableStateFlow<String?>(null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        connectionLiveData = ConnectionLiveData(requireContext())
        return ComposeView(requireContext()).apply {
            setContent {
                ShoryanTheme {
                    val connectionStatus = connectionLiveData.observeAsState(true).value
                    val loadingState =
                        viewModel.rewardRedeemingState.collectAsState(RedeemingRewardsViewModel.RedeemingState.LOADING).value

                    Screen(connectionStatus, loadingState)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeNavController(view)
        fetchRewardDetails()
        observeCodeVerificationStatus()
    }

    private fun initializeNavController(view: View) {
        navController = Navigation.findNavController(view)
    }

    private fun fetchRewardDetails() = lifecycleScope.launchWhenResumed {
        viewModel.getRewardDetails(rewardId)
    }

    @Composable
    fun Screen(
        connectionStatus: Boolean,
        loadingState: RedeemingRewardsViewModel.RedeemingState
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            when (loadingState) {
                RedeemingRewardsViewModel.RedeemingState.LOADING -> LoadingScreen()
                RedeemingRewardsViewModel.RedeemingState.LOADING_FAILED -> LoadingErrorScreen(
                    R.drawable.ic_refresh,
                    resources.getString(R.string.click_here_to_try_again),
                    ::fetchRewardDetails
                )
                RedeemingRewardsViewModel.RedeemingState.COMPLETED -> RedeemingCompletedScreen()
                else -> RewardScreen()
            }
            InternetConnectionBanner(requireContext(), Color.White, connectionStatus)
        }
    }

    @Composable
    fun RewardScreen() {
        val reward = viewModel.detailedReward.collectAsState().value!!
        ConstraintLayout(
            Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            val column = createRef()
            Column(
                Modifier
                    .constrainAs(column){
                        linkTo(parent.top, parent.bottom)
                        linkTo(parent.start, parent.end)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                    }
            ) {
                val isBeingRedeemed: Boolean by viewModel.isBeingRedeemed.collectAsState(false)
                ConstraintLayout(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val parentLayout = this
                    val (appBar, points, cover, logo, description, branches, button) = createRefs()
                    CoverImage(reward.store.coverLink, parentLayout, cover)
                    AppBar(reward.store.name, parentLayout, appBar)
                    if (!isBeingRedeemed) UserPoints(parentLayout, points, cover)
                    Logo(parentLayout, logo, cover, reward.store.logoLink)
                    OfferDescription(reward.description, parentLayout, description, logo)
                    Branches(reward.store.branches, parentLayout, branches, logo, isBeingRedeemed)
                    RewardRedeemingStatus(reward.points, parentLayout, branches, button, isBeingRedeemed)
                }
            }
        }

        ErrorsComposable()
    }

    @Composable
    fun ErrorsComposable() {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .height(70.dp),
        ) {
            // Collect server errors from RedeemingRewardsViewModel
            val messageToUser: ServerError? by viewModel.messagesToUser.collectAsState(null)
            // Collect errors issued by this fragment (when the user tries redeeming without
            // choosing a branch)
            val fragmentErrorMessages: String? by fragmentErrors.collectAsState(null)
            // Collect server errors from SMSViewModel
            val redeemingCodeErrors: ServerError? by smsViewModel.eventsFlow.collectAsState(null)
            messageToUser?.let {
                if(isSpecialError(it)) handleSpecialError(it)
                else ShowSnackbar(resources.getString(it.errorStringResource)) {
                    OKSnackbarButton()
                }
            }
            fragmentErrorMessages?.let {
                ShowSnackbar(it) { OKSnackbarButton() }
            }
            redeemingCodeErrors?.let {
                if(isSpecialError(it)) handleSpecialError(it)
                else ShowSnackbar(message = resources.getString(it.errorStringResource)) {
                    OKSnackbarButton()
                }
            }
        }
    }

    @Composable
    fun AppBar(
        storeName: String,
        parentLayout: ConstraintLayoutScope,
        appBar: ConstrainedLayoutReference
    ) {
        parentLayout.apply {
            AppBar(
                storeName,
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
        coverImageLink: String,
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
                CoilImage(
                    data = coverImageLink,
                    contentDescription = "Cover Image",
                    Modifier
                        .border(1.dp, Color(0xFF707070), RectangleShape)
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
                    .background(Color.White)
                    .constrainAs(pointsReference) {
                        end.linkTo(coverReference.end)
                        top.linkTo(parent.top, margin = 70.dp)
                    },
                elevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 25.dp, topStart = 25.dp)
            ) {
                Text(
                    text = resources.getString(R.string.point, viewModel.userPoints),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(25.dp, 15.dp, 15.dp, 10.dp)
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
                contentDescription = "Logo",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, Gray, CircleShape)
                    .aspectRatio(1f)
                    .constrainAs(logoReference) {
                        top.linkTo(coverReference.bottom, - AndroidUtility.getScreenWidth(requireContext()) * 0.1f)
                        start.linkTo(parent.start, margin = 30.dp)
                        width = Dimension.percent(0.25f)
                    },
            )
        }
    }

    @Composable
    fun OfferDescription(
        rewardDescription: String,
        parentLayout: ConstraintLayoutScope,
        descriptionReference: ConstrainedLayoutReference,
        logoReference: ConstrainedLayoutReference
    ) {
        parentLayout.apply {
            Text(
                text = rewardDescription,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.subtitle2,
                color = Color.Black,
                modifier = Modifier
                    .constrainAs(descriptionReference) {
                        linkTo(logoReference.top, logoReference.bottom, 0.dp, 0.dp, 0.7f)
                        linkTo(logoReference.end, parent.end, 15.dp, 15.dp)
                        width = Dimension.fillToConstraints
                }
            )
        }
    }

    @Composable
    fun Branches(
        storeBranches: List<Branch>,
        parentLayout: ConstraintLayoutScope,
        branchesReference: ConstrainedLayoutReference,
        logoReference: ConstrainedLayoutReference,
        isBeingRedeemed: Boolean
    ) {

        if(isBeingRedeemed && chosenBranchId == null){
            chosenBranchAddress = storeBranches[0].getStringAddress()
            chosenBranchId = storeBranches[0].id
        }

        if(isBeingRedeemed){
            parentLayout.apply {
                Text(
                    text = resources.getString(R.string.enter_code_branch, storeBranches[0].getStringAddress()),
                    color = MaterialTheme.colors.secondaryVariant,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.constrainAs(branchesReference) {
                        linkTo(
                            logoReference.start,
                            parent.end,
                            bias = 0f
                        )
                        linkTo(
                            logoReference.bottom,
                            parent.bottom,
                            bias = 0.125f
                        )

                        width = Dimension.percent(0.9f)
                    }
                )
            }
        }
        else{
            ChooseBranch(storeBranches, parentLayout, branchesReference, logoReference)
        }
    }

    @Composable
    fun ChooseBranch(
        storeBranches: List<Branch>,
        parentLayout: ConstraintLayoutScope,
        branchesReference: ConstrainedLayoutReference,
        logoReference: ConstrainedLayoutReference,
    ){
        parentLayout.apply {
            Column(
                modifier = Modifier
                    .constrainAs(branchesReference) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        linkTo(
                            logoReference.bottom,
                            parent.bottom,
                            bias = 0.125f
                        )
                    }
                    .fillMaxWidth()
            ) {
                Text(
                    text = resources.getString(R.string.branches),
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.Black,
                    modifier = Modifier.padding(ShoryanTheme.dimens.grid_4_5, 0.dp)
                )

                val isOpen = remember { mutableStateOf(false) }
                val openCloseOfDropDownList: (Boolean) -> Unit = {
                    isOpen.value = it
                }
                val onBranchSelected: (String, String) -> Unit = { id, address ->
                    chosenBranchId = id
                    chosenBranchAddress = address
                }

                val dropdownArrow = R.mipmap.iconfinder_nav_arrow_right_1

                Box {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                0.dp,
                                ShoryanTheme.dimens.grid_4,
                                0.dp,
                                0.dp
                            )
                    ) {
                        TextField(
                            value = chosenBranchAddress ?: resources.getString(R.string.choose_branch),
                            onValueChange = { chosenBranchAddress = it },
                            trailingIcon = {
                                Image(
                                    painter = painterResource(dropdownArrow),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .scale(2f)
                                        .padding(10.dp, 0.dp)
                                )
                            },
                            textStyle = MaterialTheme.typography.body1,
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .wrapContentHeight()
                                .clickable(false, null, null) {}
                                .border(
                                    2.dp,
                                    MaterialTheme.colors.primary,
                                    MaterialTheme.shapes.small.copy(CornerSize(ShoryanTheme.dimens.grid_3))
                                ),
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = Color(0xFFB2AAAA),
                                backgroundColor = Color.White,
                                cursorColor = Color.Transparent, // To hide the cursor and underline
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent)
                        )
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth(0.75f)
                        ){
                            DropDownComposable(
                                isOpen.value,
                                storeBranches,
                                openCloseOfDropDownList,
                                onBranchSelected,
                                AndroidUtility.getScreenWidth(requireContext()) * 0.75f
                            )
                        }

                    }
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    isOpen.value = true
                                }
                            )
                    )
                }
            }
        }
    }

    @Composable
    fun RewardRedeemingStatus(
        rewardPoints: Int,
        parentLayout: ConstraintLayoutScope,
        branches: ConstrainedLayoutReference,
        button: ConstrainedLayoutReference,
        isBeingRedeemed: Boolean
    ) {
        if (isBeingRedeemed) {
            CodeEntryUI(parentLayout, button, branches)
        } else {
            if (canUserRedeemReward(viewModel.userPoints, rewardPoints))
                RedeemButton(parentLayout, button, branches)
            else
                InsufficientPointsButton(rewardPoints, parentLayout, button, branches)
        }
    }

    @Composable
    fun CodeEntryUI(
        parentLayout: ConstraintLayoutScope,
        button: ConstrainedLayoutReference,
        branchesReference: ConstrainedLayoutReference
    ) {
        val canResendSMS = smsViewModel.canResendSMS
        val remainingTime = smsViewModel.remainingTimeString
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
                layoutDirection = getLayoutDirection(),
                modifier = Modifier
                    .constrainAs(button) {
                        linkTo(branchesReference.bottom, parent.bottom, bias = 0.5f)
                        centerHorizontallyTo(parent)
                        width = Dimension.percent(0.88f)
                    }
                    .padding(5.dp, 0.dp)
            )
        }
    }

    private fun getLayoutDirection(): Int =
        when(requireContext().resources.configuration.locale){
            Locale("ar") -> PinEntryComposableDirection.RTL
            else -> PinEntryComposableDirection.LTR
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
        rewardPoints: Int,
        parentLayout: ConstraintLayoutScope,
        buttonReference: ConstrainedLayoutReference,
        branchesReference: ConstrainedLayoutReference
    ) {
        RewardButton(
            parentLayout,
            buttonReference,
            branchesReference,
            false,
            resources.getString(R.string.points_remaining, rewardPoints - viewModel.userPoints)
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
    ) {
        parentLayout.apply {
            Button(
                onClick = onClick,
                enabled = enabled,
                contentPadding = PaddingValues(0.dp, 17.dp),
                shape = RoundedCornerShape(29.dp),
                modifier = Modifier
                    .constrainAs(buttonReference) {
                        linkTo(
                            parent.start,
                            parent.end
                        )
                        linkTo(
                            top = branchesReference.bottom,
                            bottom = parent.bottom,
                            bias = 0.8f
                        )
                        width = Dimension.percent(0.88f)
                    }
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.White
                )
            }
        }
    }


    private fun onRedeemButtonClicked() {
        if (chosenBranchAddress != null)
            showAlertDialog(rewardId)
        else {
            fragmentErrors.value = resources.getString(R.string.no_branch_chosen)
        }
    }

    private fun redeemReward(rewardId: String, branchId: String) {
        lifecycleScope.launchWhenResumed {
            viewModel.tryRedeemReward(rewardId, branchId)
        }
    }

    @Composable
    fun ShowSnackbar(message: String, button: @Composable () -> Unit) {
        Snackbar(
            action = button,
        ) {
            Text(message)
        }
    }

    @Composable
    fun OKSnackbarButton() {
        Button(
            onClick = {
                fragmentErrors.value = null
                viewModel.clearReceivedMessage()
                smsViewModel.clearReceivedEvent()
            }
        ) {
            Text(resources.getString(R.string.ok))
        }
    }


    @Composable
    fun RedeemingCompletedScreen(){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            val screenHeight = remember {
                AndroidUtility.getScreenHeight(requireContext())
            }
            val reward = viewModel.detailedReward.collectAsState().value!!

            AppBar(title = reward.store.name, modifier = Modifier.fillMaxWidth())
            Image(
                painterResource(R.drawable.ic_reward_check),
                contentDescription = "Check Mark",
                modifier = Modifier
                    .padding(0.dp, screenHeight * 0.15f, 0.dp, 0.dp)
                    .requiredHeight(screenHeight * 0.2f),
                contentScale = ContentScale.FillHeight
            )
            Text(
                text = resources.getString(R.string.success),
                modifier = Modifier.padding(0.dp, ShoryanTheme.dimens.grid_4, 0.dp, 0.dp),
                style = MaterialTheme.typography.h4, color = MaterialTheme.colors.secondaryVariant
            )
            Text(
                text = resources.getString(R.string.offer_redeemed),
                modifier = Modifier.padding(0.dp, ShoryanTheme.dimens.grid_5, 0.dp, 0.dp),
                style = MaterialTheme.typography.h5, color = MaterialTheme.colors.secondaryVariant
            )

            Button(
                onClick = { navController.popBackStack() },
                contentPadding = PaddingValues(20.dp),
                shape = RoundedCornerShape(29.dp),
                modifier = Modifier
                    .width(210.dp)
                    .padding(0.dp, ShoryanTheme.dimens.grid_5, 0.dp, 0.dp)
            ) {
                Text(
                    text = resources.getString(R.string.close),
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.White
                )
            }
        }
    }

    private fun canUserRedeemReward(userPoints: Int, rewardPoints: Int) = userPoints >= rewardPoints


    private fun showAlertDialog(id: String) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        with(builder) {
            setTitle(resources.getString(R.string.redeeming_confirmation_title))
            setMessage(resources.getString(R.string.redeeming_confirmation_body))
            setPositiveButton(resources.getString(R.string.confirm)) { _, _ -> redeemReward(id, chosenBranchId!!) }
            setNegativeButton(resources.getString(R.string.no), null)
            show()
        }
    }

    private fun verifyCode(code: String) {
        smsViewModel.verifyRedeemingCode(rewardId, chosenBranchId!!, code)
    }

    private fun sendSMS(){
        lifecycleScope.launchWhenResumed {
            smsViewModel.trySendSMS(rewardId, chosenBranchId!!)
        }
    }

    private fun observeCodeVerificationStatus() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            smsViewModel.isVerifyingCode.collect {
                if (it.name == "VERIFYING") {
                    showProcessingIndicator()
                } else{
                    if(it.name == "VERIFIED"){
                        viewModel.onRedeemingCodeVerified()
                    }
                    hideProcessingIndicator()
                }
            }
        }
    }

    private fun showProcessingIndicator() {
        val loadingFragment: DialogFragment? =
            childFragmentManager.findFragmentByTag("loading") as DialogFragment?
        if (loadingFragment == null)
            LoadingFragment(this).show(childFragmentManager, "loading")
    }

    private fun hideProcessingIndicator() {
        val loadingFragment: DialogFragment? =
            childFragmentManager.findFragmentByTag("loading") as DialogFragment?
        loadingFragment?.dismiss()
    }

    private fun isSpecialError(error: ServerError): Boolean{
        return when(error){
            ServerError.JWT_EXPIRED -> true
            ServerError.UNAUTHORIZED -> true
            else -> false
        }
    }
    private fun handleSpecialError(error: ServerError){
        if(error == ServerError.JWT_EXPIRED){
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                val response = tokensViewModel.getNewAccessToken(requireContext())
                // If an error happened when refreshing tokens, log user out
                response.error?.let{
                    forceLogOut()
                }
            }
        }
        else if(error == ServerError.UNAUTHORIZED){
            error.doErrorAction(requireContext())
        }
    }

    private fun forceLogOut(){
        Toast.makeText(requireContext(), resources.getString(R.string.re_login), Toast.LENGTH_LONG).show()
        val intent = Intent(context, LandingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onLoadingFragmentDismissed() {
        smsViewModel.stopVerifying()
    }
}
