package com.example.sharyan.ui

import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.example.sharyan.R
import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.recyclersAdapters.RequestsRecyclerAdapter
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class HomeTest {

    companion object{



        @BeforeClass
        @JvmStatic
        fun beforeClass(){
            // Setup NavController

        }
    }

    // This allows fragments to use by navGraphViewModels()
    private lateinit var navController: TestNavHostController
    private val currentAppUser = CurrentAppUser
    private var home: Home? = null

    @Before
    fun setup() {
        // ID of an O- user -> can donate to any type
        currentAppUser.id = "5fef607f58ddf300049f06cb"
        openHomeFragment()
    }

    private fun openHomeFragment(){
        // This allows fragments to use by navGraphViewModels()
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.setViewModelStore(ViewModelStore())
        navController.setGraph(R.navigation.main_nav_graph)
        home = Home()
        // Launch HomeFragment
        launchFragmentInContainer(null, R.style.Theme_AppCompat) {
            home!!.also {
                // In addition to returning a new instance of our Fragment,
                // get a callback whenever the fragment’s view is created
                // or destroyed so that we can set the NavController
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        // The fragment’s view has just been created
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }

    /**
     * Find a compatible request and donate to it, this method should be the last one to run;
     * as you will need to wait for 3 minutes to test again. Hence, I started the name with z_ to
     * let it run at the end.
     */
    @Test
    fun z_donateToCompatibleRequest() {
        // Wait for requests to load (max 5 seconds)
        onView(isRoot()).perform(waitForView(R.id.requestsRecycler, 5000))
        onView(withId(R.id.requestsRecycler)).perform(
            RecyclerViewActions
                ./*actionOnItem<RequestsRecyclerAdapter.RequestViewHolder>(
                    // click on an item having O- type and requester is Dan
                    allOf(
                        hasDescendant(withText("O-")),
                        hasDescendant(withText("Dan"))
                    ),
                    click()
                )*/
                actionOnItemAtPosition<RequestsRecyclerAdapter.RequestViewHolder>(
                    0, click()
                )
        )

        // Wait for request details to load and donateButton to be visible + enabled
        onView(isRoot()).perform(waitForView(R.id.donateButton, 5000))
        onView(withId(R.id.donateButton)).perform(clickInvisibleView)

        // Wait for confirm donation button to show
        onView(isRoot()).perform(waitForView(R.id.confirmDonationButton, 5000))
        onView(withId(R.id.confirmDonationButton)).perform(clickInvisibleView)

        // Check that donate button cannot be clicked again
        try{
            onView(isRoot()).perform(waitForView(R.id.donateButton, 5000))
            assertEquals("User can donate twice",false,true)
        }catch (e: Exception){
            assertEquals(true, true)
        }
    }

    @Test
    fun checkConfirmDonationButtonInitiallyInvisible(){
        // Wait for requests to load (max 5 seconds)
        onView(isRoot()).perform(waitForView(R.id.requestsRecycler, 5000))
        onView(withId(R.id.requestsRecycler)).perform(
            RecyclerViewActions
                .actionOnItemAtPosition<RequestsRecyclerAdapter.RequestViewHolder>(
                    0, click()
                )
        )

        // Check that visibility = View.GONE
        onView(withId(R.id.confirmDonationButton)).isGone()
    }

    @Test
    fun startDonationThenGoBackAndOpenRequestFromQuickAccess(){
        // Wait for requests to load (max 5 seconds)
        onView(isRoot()).perform(waitForView(R.id.requestsRecycler, 5000))
        onView(withId(R.id.requestsRecycler)).perform(
            RecyclerViewActions
                .actionOnItemAtPosition<RequestsRecyclerAdapter.RequestViewHolder>(
                    0, click()
                )
        )

        // Wait for request details to load and donateButton to be visible + enabled
        onView(isRoot()).perform(waitForView(R.id.donateButton, 5000))
        // Store requester name
        val requesterName = getTextViewText(withId(R.id.requesterName))
        // click donate
        onView(withId(R.id.donateButton)).perform(clickInvisibleView)

        // One second delay to make sure the response arrived from server
        Thread.sleep(1000)
        // Dismiss fragment
        // Get fragment instance
        val requestFragment = home!!.childFragmentManager.findFragmentByTag("requestDetails") as RequestFulfillmentFragment
        requestFragment.dismiss()

        // Open it from quick access menu
        onView(withId(R.id.pendingRequestCard)).perform(click())
        // Assert name is the same, therefore the correct request is opened
        assertEquals(
            "Wrong request opened",
            requesterName,
            getTextViewText(withId(R.id.requesterName))
        )

        cancelDonation()
    }

    @Test
    fun startDonationThenRestartAppAndOpenRequestFromQuickAccess(){
        // Wait for requests to load (max 5 seconds)
        onView(isRoot()).perform(waitForView(R.id.requestsRecycler, 5000))
        onView(withId(R.id.requestsRecycler)).perform(
            RecyclerViewActions
                .actionOnItemAtPosition<RequestsRecyclerAdapter.RequestViewHolder>(
                    0, click()
                )
        )

        // Wait for request details to load and donateButton to be visible + enabled
        onView(isRoot()).perform(waitForView(R.id.donateButton, 5000))
        // Store requester name
        val requesterName = getTextViewText(withId(R.id.requesterName))
        // click donate
        onView(withId(R.id.donateButton)).perform(clickInvisibleView)
        // Close app
        pressBackUnconditionally()
        // Reopen App
        openHomeFragment()
        // Open request from quick access menu, since the pending request will still be on its way
        // fom the server, keep trying to ope it until it arrives
        do {
            onView(withId(R.id.pendingRequestCard)).perform(click())
            Thread.sleep(500)
        }while (home!!.childFragmentManager.findFragmentByTag("requestDetails") == null)

        // Assert name is the same, therefore the correct request is opened
        assertEquals(
            "Wrong request opened",
            requesterName,
            getTextViewText(withId(R.id.requesterName))
        )

        cancelDonation()
    }

    /**
     * Cancels the ongoing Donation, to prevent tests from affecting each other
     */
    private fun cancelDonation(){
        onView(withId(R.id.cancelDonationButton)).perform(clickInvisibleView)
    }

    // This is a custom viewAction to be able to click a button that is not in screen bounds
    private val clickInvisibleView = object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            // The only constraints are view.isEnabled = true and view.Visibility = View.Visible
            return allOf(isEnabled(), viewVisible)
        }

        override fun getDescription(): String {
            return "click button"
        }

        override fun perform(uiController: UiController?, view: View) {
            view.performClick()
        }
    }

    // This is a custom method to extract text of TextView
    fun getTextViewText(matcher: Matcher<View?>?): String? {
        val stringHolder = arrayOf<String?>(null)
        onView(matcher).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(View::class.java)
            }

            override fun getDescription(): String {
                return "getting text from a TextView"
            }

            override fun perform(uiController: UiController, view: View) {
                stringHolder[0] = (view as TextView).text.toString()
            }
        })
        return stringHolder[0]
    }

    private val viewVisible = object : Matcher<View> {
        override fun describeTo(description: Description?) {

        }

        override fun matches(item: Any?): Boolean {
            return (item as View).isVisible
        }

        override fun describeMismatch(item: Any?, mismatchDescription: Description?) {
            mismatchDescription?.appendText(" -- View not visible -- ")
        }

        override fun _dont_implement_Matcher___instead_extend_BaseMatcher_() {

        }
    }

    fun isInvisible() = getViewAssertion(Visibility.INVISIBLE)
    fun isVisible() = getViewAssertion(Visibility.VISIBLE)
    private fun ViewInteraction.isGone() = getViewAssertion(Visibility.GONE)

    private fun getViewAssertion(visibility: Visibility): ViewAssertion? {
        return ViewAssertions.matches(withEffectiveVisibility(visibility))
    }
}