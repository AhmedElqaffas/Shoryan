package com.example.shoryan.viewmodels

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import com.example.shoryan.AndroidUtility
import com.example.shoryan.RedeemingWorker
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.di.MyApplication
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class RedeemingRewardsViewModelTest{
    private lateinit var viewmodel: RedeemingRewardsViewModel
    private lateinit var context: Context

    @Before
    fun setUp() {
        viewmodel = RedeemingRewardsViewModel(getApplicationContext())
        context = getApplicationContext<MyApplication>()

        // Setup WorkManager
        val config: Configuration = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context, config
        )
    }

    @Test
    fun getUserPointsTest() {
        assertEquals(CurrentAppUser.points, viewmodel.userPoints)
    }

    /**
     * Testing that RedeemingState becomes STARTED when redeeming begins
     */
    @Test
    fun redeemRewardStateTest() = runBlockingTest{
        viewmodel.redeemReward(
            "test",
            System.currentTimeMillis(),
            context.getSharedPreferences("testPrefs", MODE_PRIVATE)
        ).collect {
            assertEquals(
                RedeemingRewardsViewModel.RedeemingState.STARTED,
                viewmodel.rewardRedeemingState.value
            )

        }
    }

    /**
     * Testing that isBeingRedeemed becomes  =true when redeeming begins
     */
    @Test
    fun redeemRewardIsRedeemingTest() = runBlockingTest{
        viewmodel.redeemReward(
            "test",
            System.currentTimeMillis(),
            context.getSharedPreferences("testPrefs", MODE_PRIVATE)
        ).collect {
            assertEquals(
                true,
                viewmodel.isBeingRedeemed.first()
            )

        }
    }

    @Test
    fun `WorkManager WHEN worker is executed THEN the cached reward id is removed from sharedPreferences`() =
        runBlockingTest{
            val prefs = context.getSharedPreferences(AndroidUtility.SHARED_PREFERENCES, MODE_PRIVATE)
            val rewardId = "test"
            val redeemingStart = System.currentTimeMillis().toString()
            // Insert the reward id in the shared preference
            with(prefs!!.edit()) {
                putString(rewardId, redeemingStart)
                apply()
            }
            // redeem reward
            viewmodel.redeemReward(rewardId, redeemingStart.toLong(), prefs).collect{
                // Assert that reward id is initially in shared preference
                assertEquals(redeemingStart, prefs.getString(rewardId, ""))
                startRedeemingWorker(rewardId)
                // Assert that reward id is removed from shared preference
                assertNotEquals(redeemingStart, prefs.getString(rewardId, ""))
            }
    }

    @Test
    fun `WorkManager WHEN n requests enqueued THEN n rewardIDs are removed from SharedPreferences`(){
            val prefs = context.getSharedPreferences(AndroidUtility.SHARED_PREFERENCES, MODE_PRIVATE)
            val rewardIds = listOf("r1", "r2")
            val redeemingStart = System.currentTimeMillis().toString()
            // Insert the reward ids in the shared preference
            with(prefs!!.edit()) {
                rewardIds.forEach{
                    putString(it, redeemingStart)
                    apply()
                }
            }
            rewardIds.forEach {
                // Assert that reward ids are initially in shared preference
                assertEquals(redeemingStart, prefs.getString(it, ""))
            }
            rewardIds.forEach {
                startRedeemingWorker(it)
            }
            rewardIds.forEach {
                // Assert that reward ids are removed from shared preference
                assertNotEquals(redeemingStart, prefs.getString(it, ""))
            }
        }

    private fun startRedeemingWorker(rewardId: String) {
        // Define input data
        val input = workDataOf("REWARD_ID" to rewardId)
        // Create request and enqueue it in WorkManager
        val request = OneTimeWorkRequestBuilder<RedeemingWorker>()
            .setInputData(input)
            .build()
        val workManager = WorkManager.getInstance(getApplicationContext())
        workManager.enqueue(request)
    }
}