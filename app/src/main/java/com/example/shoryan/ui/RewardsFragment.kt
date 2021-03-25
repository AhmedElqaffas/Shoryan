package com.example.shoryan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.Dimension
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.shoryan.EnglishToArabicConverter
import com.example.shoryan.R
import com.example.shoryan.data.Reward
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.ui.composables.AppBar
import com.example.shoryan.ui.theme.*
import com.example.shoryan.viewmodels.RedeemingRewardsViewModel
import com.example.shoryan.viewmodels.RedeemingRewardsViewModelFactory
import dev.chrisbanes.accompanist.coil.CoilImage

class RewardsFragment : Fragment() {

    private val rewardsViewModel: RedeemingRewardsViewModel by viewModels {
        RedeemingRewardsViewModelFactory(
            RetrofitClient.getRetrofitClient().create(RetrofitBloodDonationInterface::class.java)
        )
    }

    private lateinit var navController: NavController


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    @ExperimentalFoundationApi
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View{
        return ComposeView(requireContext()).apply {
            setContent{
                ShoryanTheme{
                    RewardsScreen()
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
                .fillMaxHeight()
        ){
            AppBar(resources.getString(R.string.rewards), Modifier.fillMaxWidth())
            RewardsList(rewardsViewModel)
        }
    }

    @ExperimentalFoundationApi
    @Composable
    fun RewardsList(rewardsViewModel: RedeemingRewardsViewModel){
        val rewardsList: List<Reward> by rewardsViewModel.rewardsList.collectAsState(listOf())
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center){
            LazyVerticalGrid(
                cells = GridCells.Adaptive(150.dp),
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 30.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ){
                if(rewardsList.isEmpty()){
                    items(6){
                        RewardsShimmer()
                    }
                }
                else{
                    items(rewardsList.size) { index ->
                        Reward(rewardsList[index])
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
                    onClick = { onRewardClicked(reward) }
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
                        if(reward.points > rewardsViewModel.userPoints){
                            RewardPoints(reward.points - rewardsViewModel.userPoints)
                        }
                        else{
                            RewardRedeemable()
                        }
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
    fun RewardPoints(remainingPoints: Int){
        Text(
            text = remember {
                "\u200Fتحتاج ${EnglishToArabicConverter().convertDigits(remainingPoints.toString())} " +
                        "نقطة اضافية"
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle2,
            color = Gray
        )
    }

    @Composable
    fun RewardRedeemable(){
        Row(){
            Text(
                text = resources.getString(R.string.redeemable),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle2,
                color = DarkGreen,
            )

            Image(
                painterResource(R.drawable.ic_check),
                contentDescription = "Check Mark",
                modifier = Modifier.padding(5.dp, 0.dp, 0.dp, 0.dp )
            )
        }
    }

    private fun onRewardClicked(reward: Reward){
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
}