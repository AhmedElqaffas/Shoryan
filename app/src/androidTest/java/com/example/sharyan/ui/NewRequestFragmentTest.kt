package com.example.sharyan.ui

import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ListView
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.example.sharyan.R
import kotlinx.android.synthetic.main.fragment_new_request.*
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class NewRequestFragmentTest{

    // DEBUG THIS TEST, DO NOT RUN IT
    @Test
    fun radioButtonsInteractionInSameRow(){
      launchFragmentInContainer<NewRequestFragment>()
            // Click on AB+ radio button
            onView(withId(R.id.AB_plus))
                .perform(click())

            // Click on A+ radio button
            onView(withId(R.id.A_plus))
                .perform(click())

            // AB+ should be unchecked
            onView(withId(R.id.AB_plus))
                .check(matches(isNotChecked()))

            // A+ should be unchecked
            onView(withId(R.id.A_plus))
                .check(matches(isChecked()))
    }

    // DEBUG THIS TEST, DO NOT RUN IT
    @Test
    fun radioButtonsInteractionInDifferentRows(){
        launchFragmentInContainer<NewRequestFragment>()
        // Click on AB+ radio button
        onView(withId(R.id.B_plus))
            .perform(click())

        // Click on A+ radio button
        onView(withId(R.id.A_minus))
            .perform(click())

        // AB+ should be unchecked
        onView(withId(R.id.B_plus))
            .check(matches(isNotChecked()))

        // A+ should be unchecked
        onView(withId(R.id.A_minus))
            .check(matches(isChecked()))
    }

    // DEBUG THIS TEST, DO NOT RUN IT
    @Test
    fun incrementButtonGeneral(){
        // Test that increment button works
        val scenario = launchFragmentInContainer<NewRequestFragment>()
            var bagsNumberBeforeIncrement = -99
            scenario.onFragment {
                bagsNumberBeforeIncrement = it.getCurrentBagsCount()
            }

            onView(withId(R.id.incrementBloodBags))
                .perform(NestedScrollViewExtension())
                .perform(click())
            onView(withId(R.id.bagsNumberEditText))
                .check(matches(withText((bagsNumberBeforeIncrement + 1).toString())))
    }

    // DEBUG THIS TEST, DO NOT RUN IT
    @Test
    fun incrementButtonLimit(){
        // Test that increment button works
        launchFragmentInContainer<NewRequestFragment>()
        onView(withId(R.id.bagsNumberEditText))
            .perform(NestedScrollViewExtension())
            .perform(typeText("99"), closeSoftKeyboard())
        onView(withId(R.id.incrementBloodBags))
            .perform(click())
        // The value shouldn't exceed 99
        onView(withId(R.id.bagsNumberEditText))
            .check(matches(withText("99")))
    }

    @Test
    fun decrementButtonGeneral(){
        launchFragmentInContainer<NewRequestFragment>()
        onView(withId(R.id.bagsNumberEditText))
            .perform(NestedScrollViewExtension())
            .perform(typeText("11"), closeSoftKeyboard())
        onView(withId(R.id.decrementBloodBags))
            .perform(click())
        // Should now be 10
        onView(withId(R.id.bagsNumberEditText))
            .check(matches(withText("10")))
        onView(withId(R.id.decrementBloodBags))
            .perform(click())
        // Should now be 9
        onView(withId(R.id.bagsNumberEditText))
            .check(matches(withText("9")))
    }

    // DEBUG THIS TEST, DO NOT RUN IT
    /*
        Need to make sure that if the user decrements the edittext when it is still empty, no errors
        nor negative values are shown
     */
    @Test
    fun decrementButtonDontReachNegative(){
        // Test that increment button works
        launchFragmentInContainer<NewRequestFragment>()

        onView(withId(R.id.decrementBloodBags))
            .perform(NestedScrollViewExtension())
            .perform(click())
        // Should stay empty (showing the hint, not decremented to -1)
        onView(withId(R.id.bagsNumberEditText))
            .check(matches(withText("")))
    }

    // DEBUG THIS TEST, DO NOT RUN IT
    /*
        Need to make sure when user enters a number and starts decrementing it, it doesn't reach 0
     */
    @Test
    fun decrementButtonDontReachZero(){
        launchFragmentInContainer<NewRequestFragment>()
        onView(withId(R.id.bagsNumberEditText))
            .perform(NestedScrollViewExtension())
            .perform(typeText("1"), closeSoftKeyboard())
        onView(withId(R.id.decrementBloodBags))
            .perform(click())
        // Should stay empty (showing the hint, not decremented to -1)
        onView(withId(R.id.bagsNumberEditText))
            .check(matches(withText("1")))
    }

    // DEBUG THIS TEST, DO NOT RUN IT
    @Test
    fun spinnersInitialState(){
        // The government spinner should be set, the city and blood bank adapters shouldn't
        val scenario = launchFragmentInContainer<NewRequestFragment>()
        scenario.onFragment {
            assertTrue(it.spinnerGov.adapter != null)
            assertTrue(it.spinnerCity.adapter == null)
            assertTrue(it.spinnerBloodBank.adapter == null)
        }
    }

    // DEBUG THIS TEST, DO NOT RUN IT
    @Test
    fun spinnersStateAfterSelectingGovernment(){
        // city spinner adapter should be set, blood bank adapter shouldn't
        val scenario = launchFragmentInContainer<NewRequestFragment>()
        onView(withId(R.id.spinnerGov))
            .perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("قنا"))).perform(click())
        scenario.onFragment {
            assertTrue(it.spinnerCity.adapter != null)
            assertTrue(it.spinnerBloodBank.adapter == null)
        }
    }

    // DEBUG THIS TEST, DO NOT RUN IT
    @Test
    fun spinnersStateAfterSelectingGovernmentAndCity(){
        // blood bank spinner adapter should be set
        val scenario = launchFragmentInContainer<NewRequestFragment>()
        onView(withId(R.id.spinnerGov))
            .perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("قنا"))).perform(click())
        onView(withId(R.id.spinnerCity))
            .perform(click())
        onData(`is`(instanceOf(String::class.java))).atPosition(1).perform(click())
        scenario.onFragment {
            /* Blood bank adapter not implemented yet
            assertTrue(it.spinnerBloodBank.adapter == null)
             */
        }
    }

    class NestedScrollViewExtension(scrollToAction: ViewAction = scrollTo()) : ViewAction by scrollToAction {
        override fun getConstraints(): Matcher<View> {
            return Matchers.allOf(
                withEffectiveVisibility(Visibility.VISIBLE),
                isDescendantOfA(Matchers.anyOf(isAssignableFrom(
                    NestedScrollView::class.java),
                    isAssignableFrom(ScrollView::class.java),
                    isAssignableFrom(HorizontalScrollView::class.java),
                    isAssignableFrom(ListView::class.java))))
        }
    }

}

