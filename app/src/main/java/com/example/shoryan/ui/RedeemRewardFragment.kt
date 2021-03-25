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
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
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

class RedeemRewardFragment : Fragment() {
    private val viewModel: RedeemingRewardsViewModel by viewModels {
        RedeemingRewardsViewModelFactory(
            RetrofitClient.getRetrofitClient().create(RetrofitBloodDonationInterface::class.java)
        )
    }

    val sharedPref by lazy {
        activity?.getPreferences(Context.MODE_PRIVATE)
    }
    private var currentRedeeming: String? = null
    private val reward: Reward by lazy{
        requireArguments().get("reward") as Reward
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
            currentRedeeming = sharedPref!!.getString(reward.id, null)
            currentRedeeming?.let{
                viewModel.currentRewardRedeemingStartTime = it.toLong()
            }
        return ComposeView(requireContext()).apply {
            setContent {
                ShoryanTheme {
                    RewardScreen()
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
                ConstraintLayout(Modifier.fillMaxSize()) {
                    val parentLayout = this
                    val ( appBar, points, cover, logo, description, branches, button,
                    timer) = createRefs()
                    CoverImage(parentLayout, cover)
                    AppBar(parentLayout, appBar)
                    RewardPoints(parentLayout, points, cover)
                    Logo(parentLayout, logo, cover, "https://homepages.cae.wisc.edu/~ece533/images/zelda.png")
                    OfferDescription(parentLayout, description, logo)
                    Branches(parentLayout, branches, logo)
                    RewardRedeemingStatus(parentLayout, branches, button, timer)
                }
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
    fun RewardPoints(
        parentLayout: ConstraintLayoutScope,
        pointsReference: ConstrainedLayoutReference,
        coverReference: ConstrainedLayoutReference
    ) {
        parentLayout.apply {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomEnd = 25.dp, topEnd = 25.dp))
                    .background(White)
                    .padding(25.dp, 15.dp, 15.dp, 10.dp)
                    .constrainAs(pointsReference) {
                        start.linkTo(coverReference.start)
                        top.linkTo(parent.top, margin = 70.dp)
                    }
            ) {
                Text(
                    text = resources.getString(R.string.point, 2000),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.subtitle1,
                    color = Black,
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
                        end.linkTo(parent.end, margin = 30.dp)
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
                    text = "احصل على 20 جنيه خصم فوري",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.subtitle2,
                    color = Black,
                    modifier = Modifier.constrainAs(descriptionReference){
                        centerVerticallyTo(logoReference)
                        end.linkTo(logoReference.start, margin = 15.dp)
                    }
                )
            }
        }

        @Composable
        fun Branches(parentLayout: ConstraintLayoutScope,
                     branchesReference: ConstrainedLayoutReference,
                     logoReference: ConstrainedLayoutReference) {

            parentLayout.apply{
                Text(
                    text = resources.getString(R.string.branches),
                    style = MaterialTheme.typography.subtitle1,
                    color = Black,
                    modifier = Modifier.constrainAs(branchesReference){
                        end.linkTo(logoReference.end)
                        top.linkTo(logoReference.bottom, 40.dp)
                    }
                )
            }
        }

    @Composable
    fun RewardRedeemingStatus(
        parentLayout: ConstraintLayoutScope,
        branches: ConstrainedLayoutReference,
        button: ConstrainedLayoutReference,
        timer: ConstrainedLayoutReference
    ){

        val isBeingRedeemed: Boolean by viewModel.isBeingRedeemed.observeAsState(false)
        if(isBeingRedeemed){
            RemainingTime(parentLayout, timer, branches)
        }
        else{
            RedeemButton(parentLayout, button, branches)
        }
    }

        @Composable
        fun RedeemButton(
            parentLayout: ConstraintLayoutScope,
            buttonReference: ConstrainedLayoutReference,
            branchesReference: ConstrainedLayoutReference
        ) {
            parentLayout.apply{
                Button(
                    onClick = {
                        with(sharedPref!!.edit()) {
                            val redeemingStartTime = System.currentTimeMillis()
                            putString(reward.id, redeemingStartTime.toString())
                            apply()
                            viewModel.currentRewardRedeemingStartTime = redeemingStartTime
                    }},
                    contentPadding = PaddingValues(20.dp),
                    shape = RoundedCornerShape(29.dp),
                    modifier = Modifier
                        .width(350.dp)
                        .constrainAs(buttonReference) {
                            top.linkTo(branchesReference.bottom)
                            centerHorizontallyTo(parent)
                        }
                ){
                    Text(
                        text = resources.getString(R.string.redeem),
                        style = MaterialTheme.typography.subtitle1,
                        color = White
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
                    }
            ){
                val remainingTime: String by viewModel.remainingTimeString.observeAsState("")
                Text(
                    text = remainingTime,
                    style = MaterialTheme.typography.h4,
                    color = Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.requiredHeight(10.dp))

            }
        }
    }

    /*private fun redeemingTimedout(): Boolean{
        if(currentRedeeming == null){
            return true
        }
        else{
            val redeemingStartTime = currentRedeeming!!.toLong()
            return  redeemingStartTime + redeemingDuration <= System.currentTimeMillis()
        }
    }*/
}