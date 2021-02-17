package com.example.shoryan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.shoryan.viewmodels.RedeemingRewardsViewModel
import com.example.shoryan.viewmodels.RedeemingRewardsViewModelFactory
import dev.chrisbanes.accompanist.coil.CoilImage

class RewardsFragment : Fragment() {

    private val rewardsViewModel: RedeemingRewardsViewModel by viewModels {
        RedeemingRewardsViewModelFactory(
            RetrofitClient.getRetrofitClient().create(RetrofitBloodDonationInterface::class.java)
        )
    }

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

    @Composable
    fun RewardsScreen(){
            BackgroundImage()
            Column(Modifier.fillMaxWidth().fillMaxHeight()){
                AppBar(resources.getString(R.string.rewards))
                RewardsList(rewardsViewModel)
        }
    }

    @Composable
    fun BackgroundImage(){
        Image(imageFromResource(resources, R.drawable.wooden_bg),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            contentDescription = null
        )
    }


    /*@ExperimentalFoundationApi
    @Composable
    fun RewardsList(rewardsViewModel: RedeemingRewardsViewModel){
        val rewardsList: List<Reward> by rewardsViewModel.rewardsList.collectAsState(listOf())
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center){
            LazyVerticalGrid(
                cells = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 30.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ){
                if(rewardsList.isEmpty()){
                    items(5){
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
    }*/

    @Composable
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
    }

    @Composable
    fun Reward(reward: Reward){
        Card(
            backgroundColor = Color.Transparent,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.clickable(
                interactionState = InteractionState(),
                indication = null,
                onClick = {showToast(reward.points.toString())}
            )
                .width(250.dp)
                .fillMaxHeight(0.95f)
                .padding(20.dp),
            elevation = 12.dp
        ) {

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ){
                RewardImage(reward.imageLink)
                Column{
                    RewardTitle(reward.rewardName)
                    RewardPoints(reward.points)
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
            modifier = Modifier.clip(MaterialTheme.shapes.large),
        )
    }

    @Composable
    fun RewardTitle(rewardName: String){
        TextWithBackground(
            backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.7f),
            text = rewardName,
            textColor = Color.White
        )
    }

    @Composable
    fun TextWithBackground(backgroundColor: Color, text: String, textColor: Color){
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6,
            color = textColor,
            modifier = Modifier.fillMaxWidth()
                .background(backgroundColor, shape = RoundedCornerShape(5.dp))
        )
    }

    @Composable
    fun RewardPoints(points: Int){
        PointsProgress(points, rewardsViewModel.userPoints)
    }

    @Composable
    private fun PointsProgress(requiredPoints: Int, userPoints: Int){
        Box(contentAlignment = Alignment.Center)
        {
            val height = 20f
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(height.dp).scale(scaleY = (height/3f), scaleX = 1f),
                progress = remember(calculation = {getPointsPercentage(requiredPoints, userPoints)}),
                color = Color.Green,
                backgroundColor = Color.White
            )
            Text(
                text = remember {
                    "\u200F النقاط: ${EnglishToArabicConverter().convertDigits(userPoints.toString())} \\ " +
                            EnglishToArabicConverter().convertDigits(requiredPoints.toString())
                },
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.arial))
                    )
            )
        }
    }

    private fun showToast(message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun getPointsPercentage(requiredPoints: Int, userPoints: Int): Float{
        return try{
                val percentage = (userPoints * 1.0 / requiredPoints * 1.0).toFloat()
                if(percentage > 1.0f) 1.0f else percentage
        }catch (e: ArithmeticException){
            0f
        }
    }

    @Composable
    fun RewardsShimmer(){
        val colorsList = listOf(
            Gray, Shimmer, Gray
        )

        val brush = linearGradient(
            colorsList,
            start = Offset(200f,200f),
            end = Offset(400f,400f),
        )

        Surface(shape = MaterialTheme.shapes.large, color = Color.Transparent) {
            Spacer(
                modifier = Modifier
                    .width(250.dp)
                    .fillMaxHeight(0.85f)
                    .padding(vertical = 0.dp, horizontal = 20.dp)
                    .background(brush = brush)
            )
        }
    }
}