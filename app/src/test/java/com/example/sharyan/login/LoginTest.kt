package com.example.sharyan.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.sharyan.ApiTestManager
import com.example.sharyan.CoroutinesTestRule
import com.example.sharyan.MockResponseFileReader
import com.example.sharyan.data.*
import com.example.sharyan.ui.LoginViewModel
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
    private val user = User(name= Name("شريف", "أشرف"),
        points=0,
        id = "60130dbd9ed8840004bcf483",
        bloodType = BloodType.ANegative,
        password = null,
        phoneNumber = null
    )

    @Before
    fun initTest(){
        server = MockWebServer()
    }

    @After
    fun shutdown(){
        server.shutdown()
    }

    @Test
    fun `login WITH credentials correct RETURN no error and initialize CurrentAppUser`(){
        // Arrange
        // Initialize server response
        server.apply {
            enqueue(MockResponse().setBody(MockResponseFileReader("login_success.json").content))
        }
        // Setup RetrofitClient
        val apiTestManager = ApiTestManager(server.url("").toString())

        // Act
        // Perform fake api call
        var loginResponse: LoginResponse? = null
        runBlocking{
            LoginViewModel().logUser("111", "111", apiTestManager.api).observeForever {
                loginResponse = it
            }
            // No idea why this delay is needed, I've disabled internet connection
            // (meaning the code is surely not using online server), but the emission is still not instantaneous
            delay(400)
        }

        // Assert
        assertTrue(loginResponse?.error == null)
        assertThat(CurrentAppUser).usingRecursiveComparison().isEqualTo(user)
    }

    @Test
    fun `login WITH credentials incorrect RETURN LoginResponse containing error and null user object`(){
        // Arrange
        // Initialize server response
        server.apply {
            enqueue(MockResponse().setBody(MockResponseFileReader("login_fail.json").content))
        }
        // Setup RetrofitClient
        val apiTestManager = ApiTestManager(server.url("").toString())

        // Act
        // Perform fake api call
        var loginResponse: LoginResponse? = null
        runBlocking{
            LoginViewModel().logUser("111", "111", apiTestManager.api).observeForever {
                loginResponse = it
            }
            delay(400)
        }

        // Assert
        assertTrue(loginResponse?.error != null && loginResponse?.user == null)
    }
}