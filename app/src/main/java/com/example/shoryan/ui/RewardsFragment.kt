package com.example.shoryan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoryan.R
import com.example.shoryan.data.Reward
import dev.chrisbanes.accompanist.coil.CoilImage

class RewardsFragment : Fragment() {
    @ExperimentalFoundationApi
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View{
        return ComposeView(requireContext()).apply {
            setContent{
                RewardsScreen()
            }
        }
    }

    @ExperimentalFoundationApi
    @Composable
    fun RewardsScreen(){
        Column{
            Banner(resources.getString(R.string.rewards))
            //Spacer(Modifier.height(30.dp))
            RewardsList(listOf(
                Reward("الشفاطة السحرية"),
                Reward("المكنسة اللهلوبة"),
                Reward("انا امير نيجيري محتاج مساعدتك"),
                Reward("اديني الباسورد و هحوطلك 99999 نقطة"),
                Reward("سماح 3 متر ترغب بالحديث معك")
            ))
        }
    }

    @Composable
    fun Banner(title: String){
        Surface(
            Modifier.fillMaxWidth(),
            color = Color(resources.getColor(R.color.colorPrimaryDark)),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
        )
        {

            BannerTitle(title)
        }
    }
    
    @Composable
    fun BannerTitle(title: String){
        Box(contentAlignment = Alignment.Center){
            Text(
                text = title,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp),
                style = TextStyle(
                    color = Color.White,
                    fontWeight =
                    FontWeight.Bold,
                    fontSize = 25.sp, fontFamily = FontFamily(Font(R.font.arial))
                )
            )
        }
    }

    @ExperimentalFoundationApi
    @Composable
    fun RewardsList(rewards: List<Reward>){
        LazyVerticalGrid(
            cells = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(top = 30.dp)
            ){
            items(rewards.size){ index ->
                Reward(rewards[index])
            }
        }
    }

    @Composable
    fun Reward(reward: Reward){
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp, vertical = 20.dp,))
        {
            RewardImage(reward.imageLink)
            RewardTitle(reward.rewardName)
        }
    }

    @Composable
    fun RewardImage(imageLink: String){
        CoilImage(
            data = imageLink,
            contentDescription = null,
            modifier = Modifier.size(150.dp).clip(RoundedCornerShape(20.dp))
        )
    }

    @Composable
    fun RewardTitle(rewardName: String) {
        Surface(
            shape = RoundedCornerShape(5.dp),
            color = Color(resources.getColor(R.color.colorPrimary)),
            modifier = Modifier.preferredWidth(150.dp))
        {
            Text(
                text = rewardName,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.arial))
                )
            )

        }
    }
}