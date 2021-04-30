package com.example.shoryan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.shoryan.ConnectionLiveData
import com.example.shoryan.R
import com.example.shoryan.data.Reward
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.ui.composables.AppBar
import com.example.shoryan.ui.composables.InternetConnectionBanner
import com.example.shoryan.ui.theme.Gray
import com.example.shoryan.ui.theme.Shimmer
import com.example.shoryan.ui.theme.ShoryanTheme
import com.example.shoryan.viewmodels.RedeemingRewardsViewModel
import com.example.shoryan.viewmodels.RedeemingRewardsViewModelFactory
import dev.chrisbanes.accompanist.coil.CoilImage

class RewardsFragment : Fragment() {

    private val rewardsViewModel: RedeemingRewardsViewModel by viewModels {
        RedeemingRewardsViewModelFactory(
            requireActivity().application,
            RetrofitClient.getRetrofitClient().create(RetrofitBloodDonationInterface::class.java)
        )
    }

    private lateinit var navController: NavController
    private lateinit var connectionLiveData: ConnectionLiveData

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        rewardsViewModel.fetchRewardsList()
    }

    @ExperimentalFoundationApi
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View{
        connectionLiveData = ConnectionLiveData(requireContext())
        return ComposeView(requireContext()).apply {
            setContent{
                ShoryanTheme{
                    val connectionStatus = connectionLiveData.observeAsState(true).value
                    Box(contentAlignment = Alignment.BottomCenter){
                        RewardsScreen()
                        InternetConnectionBanner(requireContext(), Color.White, connectionStatus)
                        ErrorSnackbar()
                    }
                }
            }
        }
    }

    @ExperimentalFoundationApi
    @Composable
    fun RewardsScreen(){
        Column(
            Modifier
                .fillMaxWidth()
        ){
            AppBar(resources.getString(R.string.rewards), Modifier.fillMaxWidth())
            RewardsList(rewardsViewModel)
        }
    }

    @ExperimentalFoundationApi
    @Composable
    fun RewardsList(rewardsViewModel: RedeemingRewardsViewModel){
        // If the rewards list is cached, its value is retrieved before this composable is drawn
        // and thus the "collectAsState" is never triggered and the list stays empty
        // therefore, we try to get the value of the cached rewardList first, before
        // setting initializing the list as an empty list
        val initialRewardsList: List<Reward>? = try{
            rewardsViewModel.rewardsList.replayCache[0]
        }catch (e: Exception){
            listOf()
        }

        val rewardsList: List<Reward>? by rewardsViewModel.rewardsList.collectAsState(initialRewardsList)
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center){
            LazyVerticalGrid(
                cells = GridCells.Adaptive(150.dp),
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 30.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ){
                if(rewardsList.isNullOrEmpty()){
                    items(6){
                        RewardsShimmer()
                    }
                }
                else{
                    items(rewardsList!!.size) { index ->
                        Reward(rewardsList!![index])
                    }
                }
            }
        }
    }

    @Composable
    fun Reward(reward: Reward){
        Column(
            modifier = Modifier
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                    onClick = { onRewardClick(reward) }
                )
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Card(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .height(200.dp)
                    .width(150.dp),
                elevation = 12.dp,
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Column(
                        modifier = Modifier
                            .height(30.dp)
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(0.dp, 0.dp, 0.dp, 8.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        RewardPoints(reward.points)
                    }
                }
                Box(
                    contentAlignment = Alignment.TopCenter
                ) {
                    RewardImage(reward.imageLink)
                }
            }
        }

    }

    @Composable
    fun RewardImage(imageLink: String){
        CoilImage(
            data = imageLink,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .height(150.dp),
        )
    }

    @Composable
    fun RewardPoints(rewardPoints: Int){
        Text(
            text = resources.getString(R.string.point, rewardPoints),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle2,
            color = Gray
        )
    }

    private fun onRewardClick(reward: Reward){
        val bundle = bundleOf(Pair("reward", reward))
        navController.navigate(R.id.action_rewardsFragment_to_redeemRewardFragment, bundle)
    }

    @Composable
    fun RewardsShimmer(){
        val colorsList = listOf(
            Gray, Shimmer, Gray
        )

        val offsetX by animateShimmer(0f, 200f)
        val endX by animateShimmer(50f, 250f)
        val offsetY by animateShimmer(0f, 1800f)
        val endY by animateShimmer(50f, 1850f)

        val brush = linearGradient(
            colorsList,
            start = Offset(offsetX,offsetY),
            end = Offset(endX,endY),
        )
        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .width(150.dp)
                .padding(10.dp)
                .height(200.dp)
                .clip(MaterialTheme.shapes.large)
                .background(brush),
            border = null,
            backgroundColor = Color.Transparent
        ) {
        }
    }

    @Composable
    private fun animateShimmer(initialValue: Float, targetValue: Float): State<Float> {
        val infiniteTransition = rememberInfiniteTransition()
        return infiniteTransition.animateFloat(
            initialValue = initialValue,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 800
                },
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    @Composable
    fun ErrorSnackbar(){
        val error = rewardsViewModel.messagesToUser.collectAsState(null)
        error.value?.let {
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
                            onClick = {
                                rewardsViewModel.clearReceivedMessage()
                                rewardsViewModel.fetchRewardsList()
                            },
                        ){
                            Text(resources.getString(R.string.try_again))
                        }
                    }
                ) {
                    Text(resources.getString(error.value!!.errorStringResource))
                }
            }
        }
    }
}