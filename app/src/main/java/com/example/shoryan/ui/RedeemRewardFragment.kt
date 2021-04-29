package com.example.shoryan.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.fragment.app.viewModels
import com.example.shoryan.R
import com.example.shoryan.data.Reward
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.ui.composables.AppBar
import com.example.shoryan.ui.theme.Gray
import com.example.shoryan.ui.theme.ShoryanTheme
import com.example.shoryan.viewmodels.RedeemingRewardsViewModel
import com.example.shoryan.viewmodels.RedeemingRewardsViewModelFactory
import dev.chrisbanes.accompanist.coil.CoilImage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.lifecycle.lifecycleScope
import com.example.shoryan.ConnectionLiveData
import com.example.shoryan.ui.composables.InternetConnectionBanner

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
                    val ( appBar, points, cover, logo, description, branches, button,
                    timer) = createRefs()
                    CoverImage(parentLayout, cover)
                    AppBar(parentLayout, appBar)
                    if(!isBeingRedeemed) UserPoints(parentLayout, points, cover)
                    Logo(parentLayout, logo, cover, reward.imageLink)
                    OfferDescription(parentLayout, description, logo)
                    Branches(parentLayout, branches, logo)
                    RewardRedeemingStatus(parentLayout, branches, button, timer, isBeingRedeemed)
                }
            }
        }

        Row(
            verticalAlignment  = Alignment.Bottom,
            modifier = Modifier
                .height(70.dp),
        ){
            val redeemingStartSuccess: RedeemingRewardsViewModel.RedeemingState
                    by viewModel.rewardRedeemingState.collectAsState(RedeemingRewardsViewModel.RedeemingState.NOT_REDEEMING)
            if(redeemingStartSuccess == RedeemingRewardsViewModel.RedeemingState.FAILED) {
                    ShowSnackbar()
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
    fun Branches(parentLayout: ConstraintLayoutScope,
                 branchesReference: ConstrainedLayoutReference,
                 logoReference: ConstrainedLayoutReference){

        parentLayout.apply{
            Column(
                modifier = Modifier.constrainAs(branchesReference){
                    start.linkTo(logoReference.start)
                    end.linkTo(parent.end)
                    top.linkTo(logoReference.bottom, 40.dp)
                }.fillMaxWidth()
            ){
                Text(
                    text = resources.getString(R.string.branches),
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.Black,
                )

                reward.branches.forEach {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp)
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
        timer: ConstrainedLayoutReference,
        isBeingRedeemed: Boolean
    ){
        if(isBeingRedeemed){
            RemainingTime(parentLayout, timer, branches)
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
        branchesReference: ConstrainedLayoutReference
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

    @Composable
    fun RemainingTime(
        parentLayout: ConstraintLayoutScope,
        timerReference: ConstrainedLayoutReference,
        branchesReference: ConstrainedLayoutReference
    ) {
        parentLayout.apply{
            Column(
                modifier = Modifier
                    .constrainAs(timerReference) {
                        top.linkTo(branchesReference.bottom, margin = 45.dp)
                        centerHorizontallyTo(parent)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                val remainingTimeString: String by viewModel.remainingTimeString.collectAsState("")
                val remainingTimeRatio: Float by viewModel.remainingTimeRatio.collectAsState(1f)
                Text(
                    text = remainingTimeString,
                    style = MaterialTheme.typography.h4,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.requiredHeight(10.dp))
                LinearProgressIndicator(
                    progress = remainingTimeRatio,
                    backgroundColor = Color.White,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(5.dp))
                        .requiredWidth(350.dp)
                        .scale(1f, 4f)
                        .requiredHeight(14.dp)
                )
            }
        }
    }

    private fun onRedeemButtonClicked(){
        showAlertDialog(reward.id)
    }

    private fun redeemReward(rewardId: String, redeemingStartTime: Long) {
        lifecycleScope.launchWhenResumed {
            viewModel.redeemReward(rewardId, redeemingStartTime, sharedPref!!)
        }
    }

    @Composable
    fun ShowSnackbar(){
        Snackbar(
          action = {SnackbarButton()},
        ) {
            Text(resources.getString(R.string.connection_error))
        }
    }

    @Composable
    fun SnackbarButton(){
        Button(
            onClick = {
                redeemReward(reward.id, System.currentTimeMillis())
                viewModel.clearReceivedEvent()
            }
        ){
            Text(resources.getString(R.string.try_again))
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
