package com.example.shoryan.ui

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.*
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.example.shoryan.R
import com.example.shoryan.data.CurrentSession
import com.example.shoryan.ui.recyclersAdapters.RequestsRecyclerAdapter
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class HomeFragmentTest {

    // This allows fragments to use navGraphViewModels()
    private lateinit var navController: TestNavHostController
    private val currentSession = CurrentSession
    private var home: HomeFragment? = null

    @Before
    fun setup() {
        // ID of an O- user -> can donate to any type
        currentSession.user?.id = "5fef607f58ddf300049f06cb"
        openHomeFragment()
    }

    private fun openHomeFragment(){
        // This allows fragments to use 'by navGraphViewModels()'
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.setViewModelStore(ViewModelStore())
        navController.setGraph(R.navigation.main_nav_graph)
        home = HomeFragment()
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
        }catch (e: Exception) {
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

        // Check that button is invisible
        try{
            onView(allOf(withId(R.id.confirmDonationButton), viewVisible)).perform(clickInvisibleView)
            assertEquals("View is visible", false, true)
        }catch (e: Exception){
            assertEquals(true, true)
        }
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
        val requestFragment = home!!.childFragmentManager.findFragmentByTag("requestDetails") as RequestDetailsFragment
        requestFragment.dismiss()

        // Open it from quick access menu
        onView(withId(R.id.pendingRequestCard)).perform(click())
        // Wait for request details to reload
        onView(isRoot()).perform(waitForView(R.id.requesterName, 5000))
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

    @Test
    fun cantAcceptMultipleRequests(){
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
        // click donate
        onView(withId(R.id.donateButton)).perform(clickInvisibleView)
        // Wait until the server confirms starting the donation
        onView(isRoot()).perform(waitForView(R.id.confirmDonationButton,5000))
            .check { view, _ -> assertEquals(view.isVisible, true) }
        // Dismiss fragment
        pressBack()
        // Open Another request
        onView(withId(R.id.requestsRecycler)).perform(
            RecyclerViewActions
                .actionOnItemAtPosition<RequestsRecyclerAdapter.RequestViewHolder>(1, click())
        )

        try{
            // Wait for request details to load and donateButton to be visible + enabled
            onView(isRoot()).perform(waitForView(R.id.donateButton, 5000)).perform(clickInvisibleView)
            assertEquals("Can accept multiple requests", false, true)
        }catch (e: Exception){
            assertEquals(true, true)
        }
        // Dismiss fragment
        pressBack()
        // Reopen first request to cancel it
        onView(withId(R.id.requestsRecycler)).perform(
            RecyclerViewActions
                .actionOnItemAtPosition<RequestsRecyclerAdapter.RequestViewHolder>(0, click())
        )
        cancelDonation()
    }

    /**
     * Cancels the ongoing Donation, to prevent tests from affecting each other
     */
    private fun cancelDonation(){
        onView(isRoot()).perform(waitForView(R.id.cancelDonationButton, 5000))
        onView(withId(R.id.cancelDonationButton)).perform(clickInvisibleView)
        onView(isRoot()).perform(waitForView(R.id.donateButton, 5000))
            .check{view, _ -> assertEquals(view.isVisible, true)}
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
}