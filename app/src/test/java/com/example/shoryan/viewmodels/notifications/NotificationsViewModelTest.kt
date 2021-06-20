package com.example.shoryan.viewmodels.notifications

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.shoryan.ApiTestManager
import com.example.shoryan.CoroutinesTestRule
import com.example.shoryan.MockResponseFileReader
import com.example.shoryan.TimestampToElapsedTime
import com.example.shoryan.viewmodels.NotificationsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@RunWith(JUnit4::class)
class NotificationsViewModelTest{
    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesRule = CoroutinesTestRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var server : MockWebServer
    private lateinit var viewmodel: NotificationsViewModel

    @Before
    fun setUp() {
        server = MockWebServer()
        // Setup RetrofitClient
        val apiTestManager = ApiTestManager(server.url("").toString())
        // Setup ViewModel and repo
        viewmodel = NotificationsViewModel(
            NotificationsRepo_test(apiTestManager.api),
            TimestampToElapsedTime()
        )
    }

    @After
    fun shutdown(){
        server.shutdown()
    }

    @Test
    fun `getNotifications WHEN notifications are loaded THEN state is LOADED`() = runBlocking{
        // Arrange
        // Initialize server response
        server.apply {
            enqueue(MockResponse().setBody(MockResponseFileReader("notifications_success.json").content))
        }

        // Act
        // Perform fake api call
        viewmodel.getNotifications()

        // Assert
        // Check that the viewmodel notifications state is LOADED
        viewmodel.state.observeForever{
            assertEquals( viewmodel.LOADED, it)
        }
    }

    @Test
    fun `getNotifications WHEN error is returned THEN state is Error`() = runBlocking{
        // Arrange
        // Initialize server response
        server.apply {
            enqueue(MockResponse().setBody(MockResponseFileReader("notifications_fail.json").content))
        }

        // Act
        // Perform fake api call
        viewmodel.getNotifications()

        // Assert
        // Check that the viewmodel notifications state is ERROR
        viewmodel.state.observeForever{
            assertEquals( viewmodel.ERROR, it)
        }
    }
}