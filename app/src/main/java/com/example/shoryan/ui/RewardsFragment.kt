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
import androidx.fragment.app.viewModels
import com.example.shoryan.EnglishToArabicConverter
import com.example.shoryan.R
import com.example.shoryan.data.Reward
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.ui.composables.AppBar
import com.example.shoryan.ui.theme.Gray
import com.example.shoryan.ui.theme.Shimmer
import com.example.shoryan.ui.theme.ShoryanTheme
import com.example.shoryan.ui.theme.shimmer
import com.example.shoryan.viewmodels.RedeemingRewardsViewModel
import com.example.shoryan.viewmodels.RedeemingRewardsViewModelFactory
import dev.chrisbanes.accompanist.coil.CoilImage

class RewardsFragment : Fragment() {

    private val rewardsViewModel: RedeemingRewardsViewModel by viewModels {
        RedeemingRewardsViewModelFactory(
            RetrofitClient.getRetrofitClient().create(RetrofitBloodDonationInterface::class.java)
        )
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
                    .fillMaxHeight()){
                AppBar(resources.getString(R.string.rewards))
                RewardsList(rewardsViewModel)
        }
    }

    /*@Composable
    fun BackgroundImage(){
        ImageVector
        Image(painter = painterResource(id = R.drawable.wooden_bg),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            contentDescription = null
        )
    }*/


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

    /*@Composable
    fun RewardsList(rewardsViewModel: RedeemingRewardsViewModel){
        val rewardsList: List<Reward> by rewardsViewModel.rewardsList.collectAsState(listOf())
        LazyRow(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ){
            if(rewardsList.isEmpty()){
                items(3){
                    RewardsShimmer()
                }
            }
            else{
                items(rewardsList.size) { index ->
                    Reward(rewardsList[index])
                }
            }
        }
    }*/

    @Composable
    fun Reward(reward: Reward){
        Column(
            modifier = Modifier
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                    onClick = { showToast(reward.points.toString()) }
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
                    //RewardTitle(reward.rewardName)
                    //Spacer(modifier = Modifier.height(5.dp))
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
    fun RewardTitle(rewardName: String){
        Text(
            text = rewardName,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    fun RewardPoints(requiredPoints: Int){
        val userPoints = remember{rewardsViewModel.userPoints}
        Column(
        ){
            //val (progress, pointsText) = createRefs()
            //val startGuideline = createGuidelineFromStart(0f)
            //val endGuideline = createGuidelineFromStart(0.6f)
            /*CircularProgressIndicator(
                progress = remember(calculation = {getPointsPercentage(requiredPoints, userPoints)}),
                color = MaterialTheme.colors.primaryVariant,
                strokeWidth = 15.dp,
                modifier = Modifier
                    .constrainAs(progress)
                    {
                        linkTo(start = startGuideline, end = endGuideline)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    }
                    .rotate(270f) // To start progress from left instead of top

            )*/
            Text(
                text = remember {
                    "\u200Fتحتاج ${EnglishToArabicConverter().convertDigits(userPoints.toString())} " +
                            "نقطة اضافية"
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle2,
                color = Gray
            )
        }
    }

    private fun showToast(message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun getPointsPercentage(requiredPoints: Int, userPoints: Int): Float{
        return try{
                val percentage = (userPoints * 1.0 / requiredPoints * 1.0).toFloat()
            // The *0.5 is to scale the percentage to in semi-circle progress bar instead of full-circle
                if(percentage > 1.0f) 0.5f else percentage * 0.5f
        }catch (e: ArithmeticException){
            0f
        }
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

    private val SemiCircle = GenericShape { size, _ ->
        moveTo(size.width, size.height/2f)
        arcTo(Rect(0f, 0f, size.width, size.height), 0f, -180f, true)
    }
}