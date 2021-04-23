package com.example.shoryan.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.shoryan.ApiTestManager
import com.example.shoryan.CoroutinesTestRule
import com.example.shoryan.MockResponseFileReader
import com.example.shoryan.data.*
import com.example.shoryan.viewmodels.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat

class LoginTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesRule = CoroutinesTestRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var server : MockWebServer
    private lateinit var viewModel: LoginViewModel

    @Before
    fun initTest(){
        server = MockWebServer()
    }

    @After
    fun shutdown(){
        server.shutdown()
    }

    @Test
    fun `login WITH credentials correct RETURN tokens and no error`(){
        // Arrange
        // Initialize server response
        server.apply {
            enqueue(MockResponse().setBody(MockResponseFileReader("login_success.json").content))
        }
        // Setup RetrofitClient
        val apiTestManager = ApiTestManager(server.url("").toString())
        // Setup ViewModel
        viewModel = LoginViewModel(apiTestManager.api)

        // Act
        // Perform fake api call
        var loginResponse: LoginResponse? = null
        runBlocking{
            viewModel.logUser("111", "111").observeForever {
                loginResponse = it
            }
            // No idea why this delay is needed, I've tried disabling internet connection
            // (meaning the code is surely not using online server), but the emission is still not instantaneous
            delay(400)
        }

        // Assert
        assertTrue(loginResponse?.error == null)
    }

    @Test
    fun `login WITH credentials incorrect RETURN LoginResponse containing error and null tokens`(){
        // Arrange
        // Initialize server response
        server.apply {
            enqueue(MockResponse().setBody(MockResponseFileReader("login_fail.json").content))
        }
        // Setup RetrofitClient
        val apiTestManager = ApiTestManager(server.url("").toString())
        // Setup ViewModel
        viewModel = LoginViewModel(apiTestManager.api)

        // Act
        // Perform fake api call
        var loginResponse: LoginResponse? = null
        runBlocking{
            viewModel.logUser("111", "111").observeForever {
                loginResponse = it
            }
            delay(400)
        }

        // Assert
        assertTrue(loginResponse?.error != null
                && loginResponse?.accessToken == null
                && loginResponse?.refreshToken == null)
    }
}