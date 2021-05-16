package com.example.shoryan.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.shoryan.ApiTestManager
import com.example.shoryan.CoroutinesTestRule
import com.example.shoryan.MockResponseFileReader
import com.example.shoryan.data.CurrentSession
import com.example.shoryan.data.Reward
import com.example.shoryan.viewmodels.rewards.RewardsRepo_test
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

    val fakeRedeemingRewardObject = Reward(
        "dummy id",
        "name",
        0,
        "https://homepages.cae.wisc.edu/~ece533/images/zelda.png",
        "desc" ,
        listOf("branch 1", "branch 2", "branch 3"),
        true
    )

    val fakeNotRedeemingRewardObject = fakeRedeemingRewardObject.copy(
        isBeingRedeemed = false
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
        delay(200) // To make sure that fetchRewardsList() has finished
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
        viewmodel.getRewardDetails(Reward("dummy", null, 0, "dummy", null, null))

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
            fakeRedeemingRewardObject,
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
        viewmodel.getRewardDetails(Reward("dummy", null, 0, "dummy", null, null))

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
            fakeNotRedeemingRewardObject,
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
        viewmodel.getRewardDetails(Reward("dummy", null, 0, "dummy", null, null))

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
            Reward("dummy", null, 0, "dummy", null, null),
            viewmodel.detailedReward.value
        )
    }


        /**
     * Testing that RedeemingState becomes STARTED when redeeming begins
    */
   /*@Test
    fun redeemRewardStateTest() = runBlockingTest{
        viewmodel.tryRedeemReward("test")
            .invokeOnCompletion {
            assertEquals(
                RedeemingRewardsViewModel.RedeemingState.STARTED,
                viewmodel.rewardRedeemingState.value
            )
        }
    }*/

    /**
     * Testing that isBeingRedeemed becomes  =true when redeeming begins
     */
    /*@Test
    fun redeemRewardIsRedeemingTest() = runBlockingTest{
        viewmodel.tryRedeemReward(
            "test",
            System.currentTimeMillis(),
            context.getSharedPreferences("testPrefs", MODE_PRIVATE)
        ).invokeOnCompletion {
            runBlockingTest {
                assertEquals(true, viewmodel.isBeingRedeemed.first())
            }
        }
    }*/

    /*

    @Test
    fun `redeemReward() test RedeemingState is set to STARTED`() = runBlockingTest{
        viewmodel.tryRedeemReward(
            "test",
            System.currentTimeMillis(),
            context.getSharedPreferences(AndroidUtility.SHARED_PREFERENCES, MODE_PRIVATE)
        ).invokeOnCompletion {
            assertEquals(
                RedeemingRewardsViewModel.RedeemingState.STARTED ,
                viewmodel.rewardRedeemingState.value
            )
        }
    }
*/
}