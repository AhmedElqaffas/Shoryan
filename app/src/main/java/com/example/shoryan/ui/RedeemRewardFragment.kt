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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.shoryan.ConnectionLiveData
import com.example.shoryan.R
import com.example.shoryan.data.Reward
import com.example.shoryan.data.ServerError
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.ui.composables.AppBar
import com.example.shoryan.ui.composables.DropDownComposable
import com.example.shoryan.ui.composables.InternetConnectionBanner
import com.example.shoryan.ui.theme.Gray
import com.example.shoryan.ui.theme.ShoryanTheme
import com.example.shoryan.viewmodels.RedeemingRewardsViewModel
import com.example.shoryan.viewmodels.RedeemingRewardsViewModelFactory
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.flow.MutableStateFlow

class RedeemRewardFragment : Fragment() {
    private val viewModel: RedeemingRewardsViewModel by viewModels {
        RedeemingRewardsViewModelFactory(
            requireActivity().application,
            RetrofitClient.getRetrofitClient().create(RetrofitBloodDonationInterface::class.java)
        )
    }
    private lateinit var connectionLiveData: ConnectionLiveData
    private val sharedPref by lazy {
        activity?.applicationContext?.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    }
    private var currentRedeeming: String? = null
    private val reward: Reward by lazy{
        requireArguments().get("reward") as Reward
    }
    private var chosenBranch: String? = null
    // Errors to show to user
    private val fragmentErrors = MutableStateFlow<String?>(null)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
            connectionLiveData = ConnectionLiveData(requireContext())
            currentRedeeming = sharedPref!!.getString(reward.id, null)
            currentRedeeming?.let{
                viewModel.setRedeemingStartTime(it.toLong(), it, sharedPref!!)
            }
        return ComposeView(requireContext()).apply {
            setContent {
                ShoryanTheme {
                    val connectionStatus = connectionLiveData.observeAsState(true).value
                    Box(contentAlignment = Alignment.BottomCenter) {
                        RewardScreen()
                        InternetConnectionBanner(
                            requireContext(),
                            Color.White,
                            connectionStatus
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun RewardScreen() {
        val scrollState = rememberScrollState()
        ConstraintLayout(
            Modifier.fillMaxSize()
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
                    Branches(parentLayout, branches, logo)
                    RewardRedeemingStatus(parentLayout, branches, button, isBeingRedeemed)
                }
            }
        }

        Row(
            verticalAlignment  = Alignment.Bottom,
            modifier = Modifier
                .height(70.dp),
        ){
            val messageToUser: ServerError? by viewModel.messagesToUser.collectAsState(null)
            val fragmentErrorMessages: String? by fragmentErrors.collectAsState(null)
            messageToUser?.let {
                ShowSnackbar(resources.getString(it.errorStringResource)) { TryAgainSnackbarButton() }
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
        logoReference: ConstrainedLayoutReference
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
                                onClick = { isOpen.value = true }
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
          //  RemainingTime(parentLayout, timer, branches)
        }
        else{
            if(canUserRedeemReward())
                RedeemButton(parentLayout, button, branches)
            else
                InsufficientPointsButton(parentLayout, button, branches)
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

    private fun redeemReward(rewardId: String, redeemingStartTime: Long) {
        lifecycleScope.launchWhenResumed {
            viewModel.tryRedeemReward(rewardId, redeemingStartTime, sharedPref!!)
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
                redeemReward(reward.id, System.currentTimeMillis())
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
            setPositiveButton(resources.getString(R.string.confirm)) { _, _ -> redeemReward(id, System.currentTimeMillis()) }
            setNegativeButton(resources.getString(R.string.no),null)
            show()
        }
    }
}
