package com.example.shoryan.viewmodels.rewards

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.shoryan.ApiTestManager
import com.example.shoryan.CoroutinesTestRule
import com.example.shoryan.MockResponseFileReader
import com.example.shoryan.data.*
import com.example.shoryan.viewmodels.RedeemingRewardsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.mock


@RunWith(JUnit4::class)
class RedeemingRewardsViewModelTest{

    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesRule = CoroutinesTestRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var server : MockWebServer
    private lateinit var viewmodel: RedeemingRewardsViewModel
    private lateinit var context: Context

    private val fakeReward= Reward(
        "dummy id",
        0,
        "name",
        Store(
            "dummy",
            "dummy",
            "dummy",
            "dummy",
            listOf(Branch("dummy", Location("","",null,null)))
        )
    )

    private val fakeRedeemingReward = RewardResponse(
        null,
        SuccessfulRewardResponse(fakeReward, true)
    )

    private val fakeNotRedeemingRewardObject = fakeRedeemingReward.copy(
        successfulRewardResponse = SuccessfulRewardResponse(fakeReward, false)
    )


    @Before
    fun setUp() {
        server = MockWebServer()
        context = mock(Context::class.java)
        // Setup RetrofitClient
        val apiTestManager = ApiTestManager(server.url("").toString())
        // Setup ViewModel and repo
        viewmodel = RedeemingRewardsViewModel(context, RewardsRepo_test(apiTestManager.api))
    }

    @After
    fun shutdown(){
        server.shutdown()
    }

    @Test
    fun getUserPointsTest() {
        assertEquals(CurrentSession.user?.points ?: 0, viewmodel.userPoints)
    }

    @Test
    fun `getRewardsList WHEN no server error RETURN rewards list and no errors`() = runBlocking{
        // Arrange
        // Initialize server response
        server.apply {
            enqueue(MockResponse().setBody(MockResponseFileReader("rewardsList_success.json").content))
        }

        // Act
        // Perform fake api call
        viewmodel.fetchRewardsList()

        // Assert
        // Check that the viewmodel rewards list contains the rewards
        // We will check that the list size is equal to three (since the fake json file has 3 rewards)
        delay(200) // To make sure that fetchRewardsList() has finished
        assertEquals("Rewards List not populated", 3, viewmodel.rewardsList.replayCache[0]?.size)
        assertEquals("Response has error as well", null, viewmodel.messagesToUser.replayCache[0])

    }

    @Test
    fun `getRewardsList WHEN exists server error RETURN null rewards list and an error`() = runBlocking{
        // Arrange
        // Initialize server response
        server.apply {
            enqueue(MockResponse().setBody(MockResponseFileReader("rewardsList_fail.json").content))
        }

        // Act
        // Perform fake api call
        viewmodel.fetchRewardsList()

        // Assert
        // Check that the rewardsList is null and messagesToUser contains a message
        delay(300) // To make sure that fetchRewardsList() has finished
        assertEquals("Rewards List should be empty", null, viewmodel.rewardsList.replayCache[0]?.size)
        // The "CONNECTION_ERROR" is used since it is the one specified in the fake json file
        assertEquals("Response should have error", "CONNECTION_ERROR", viewmodel.messagesToUser.replayCache[0]?.name)
    }

    @Test
    fun `getRewardDetails WITH successful response and reward being redeemed THEN set redeeming state to 'Started' and store reward details`() = runBlocking{
        server.apply {
            enqueue(MockResponse().setBody(MockResponseFileReader("rewardDetails_success_beingRedeemed.json").content))
        }

        // Act
        // Perform fake api call
        viewmodel.getRewardDetails("rewardId")

        // Assert
        /* assert that
           1 - rewardRedeemingState = RedeemingState.STARTED
           2 - isBeingRedeemed = true
           3 - detailedReward contains the reward details
         */
        assertEquals("state should be STARTED",
            RedeemingRewardsViewModel.RedeemingState.STARTED,
            viewmodel.rewardRedeemingState.value
        )

        assertEquals(true, viewmodel.isBeingRedeemed.first())
        assertEquals(
            "Reward details not updated",
            fakeRedeemingReward.successfulRewardResponse?.reward,
            viewmodel.detailedReward.value
        )

    }

    @Test
    fun `getRewardDetails WITH successful response and reward not being redeemed THEN set redeeming state to 'Not_redeeming' and store reward details`() = runBlocking {
        server.apply {
            enqueue(MockResponse().setBody(MockResponseFileReader("rewardDetails_success_notBeingRedeemed.json").content))
        }

        // Act
        // Perform fake api call
        viewmodel.getRewardDetails("rewardId")

        // Assert
        /* assert that
           1 - rewardRedeemingState = RedeemingState.NOT_REDEEMING
           2 - isBeingRedeemed = false
           3 - detailedReward contains the reward details
         */
        assertEquals(
            "state should be NOT_REDEEMING",
            RedeemingRewardsViewModel.RedeemingState.NOT_REDEEMING,
            viewmodel.rewardRedeemingState.value
        )

        assertEquals(false, viewmodel.isBeingRedeemed.first())
        assertEquals(
            "Reward details not updated",
            fakeNotRedeemingRewardObject.successfulRewardResponse?.reward,
            viewmodel.detailedReward.value
        )
    }

    @Test
    fun `getRewardDetails WITH unsuccessful response THEN set redeeming state to 'Loading_failed'`() = runBlocking {
        server.apply {
            enqueue(MockResponse().setBody(MockResponseFileReader("rewardDetails_fail.json").content))
        }

        // Act
        // Perform fake api call
        viewmodel.getRewardDetails("rewardId")

        // Assert
        /* assert that
           1 - rewardRedeemingState = RedeemingState.LOADING_FAILED
           2 - isBeingRedeemed = false
           3 - detailedReward contains the initial reward object passed to the getRewardDetails() method
         */
        assertEquals(
            "state should be LOADING_FAILED",
            RedeemingRewardsViewModel.RedeemingState.LOADING_FAILED,
            viewmodel.rewardRedeemingState.value
        )

        assertEquals(false, viewmodel.isBeingRedeemed.first())
        assertEquals(
            null,
            viewmodel.detailedReward.value
        )
    }

   @Test
    fun `startRedeemingReward WHEN successfully started THEN set RedeemingState = 'Started'`() = runBlocking{
        server.apply {
            enqueue(MockResponse().setBody(MockResponseFileReader("startRewardRedeeming_success.json").content))
        }

        // Act
        // Perform fake api call
        viewmodel.tryRedeemReward("rewardId", "branchId")

        // Assert
        // Make sure RedeemingState = STARTED, and isBeingRedeemed = true
        assertEquals(
            RedeemingRewardsViewModel.RedeemingState.STARTED,
            viewmodel.rewardRedeemingState.value
        )
        assertEquals(true, viewmodel.isBeingRedeemed.first())
    }

    @Test
    fun `startRedeemingReward WHEN failed to start THEN set RedeemingState = 'Redeeming_failed'`() = runBlocking{
        server.apply {
            enqueue(MockResponse().setBody(MockResponseFileReader("startRewardRedeeming_fail.json").content))
        }

        // Act
        // Perform fake api call
        viewmodel.tryRedeemReward("rewaredId", "branchId")

        // Assert
        // Make sure RedeemingState = REDEEMING_FAILED, and isBeingRedeemed = false
        assertEquals(
            RedeemingRewardsViewModel.RedeemingState.REDEEMING_FAILED,
            viewmodel.rewardRedeemingState.value
        )
        assertEquals(false, viewmodel.isBeingRedeemed.first())
    }

    @Test
    fun `onCodeVerified THEN set redeemingState to 'Completed'`(){
        viewmodel.onRedeemingCodeVerified()
        assertEquals(
            RedeemingRewardsViewModel.RedeemingState.COMPLETED,
            viewmodel.rewardRedeemingState.value
        )
    }

    @Test
    fun `onMessageReceived THEN set messagesToUser value to null`(){
        viewmodel.clearReceivedMessage()
        assertEquals(
            null,
            viewmodel.messagesToUser.replayCache[0]
        )
    }
}