package com.example.sharyan.repos

import com.example.sharyan.data.Name
import com.example.sharyan.data.User
import com.example.sharyan.data.UserStateWrapper
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import org.junit.runner.RunWith
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.assertj.core.api.Assertions.assertThat


@RunWith(MockitoJUnitRunner::class)

class UsersRetrieverTest {

    @Spy
    lateinit var usersRetriever: UsersRetriever

    private var correctUserWrapper = UserStateWrapper(
        User("a", Name("Ahmed"), "01097049699", "pass1"),
        null
    )

    var incorrectUserWrapper = UserStateWrapper(
        null,
        "خطأ في رقم الهاتف او كلمة السر"
    )

    private val user = User("a", Name("Ahmed"), "01097049699", "pass1")
    private val list = listOf(user)
    private val correctPhoneNumber = "01097049699"
    private val correctPassword = "pass1"
    private val incorrectPhoneNumber = "010804655555"
    private val incorrectPassword = "wrongPass"

    @Test
    fun `verifyCredentials() WITH user found RETURN user wrapper with non-null user`(){
        // Arrange
        doReturn(usersRetriever.verifyCredentials(list, correctPhoneNumber, correctPassword))
            .`when`(usersRetriever).verifyCredentials(list, correctPhoneNumber, correctPassword)

        // Act
        val returnedObject = usersRetriever.verifyCredentials(list, correctPhoneNumber, correctPassword)

        // Assert
        assertThat(returnedObject).usingRecursiveComparison().isEqualTo(correctUserWrapper)
    }

    @Test
    fun `verifyCredentials() WITH user found RETURN user wrapper with null error`(){
        // Arrange
        doReturn(usersRetriever.verifyCredentials(list, correctPhoneNumber, correctPassword))
            .`when`(usersRetriever).verifyCredentials(list, correctPhoneNumber, correctPassword)

        // Act
        val returnedObject = usersRetriever.verifyCredentials(list, correctPhoneNumber, correctPassword)

        // Assert
        assertEquals("Error is not null but user is found", returnedObject.error, null )
    }

    @Test
    fun `verifyCredentials() WITH user not found RETURN user wrapper with null user and non-null error`(){
        // Arrange
        doReturn(usersRetriever.verifyCredentials(list, incorrectPhoneNumber, correctPassword))
            .`when`(usersRetriever).verifyCredentials(list, incorrectPhoneNumber, correctPassword)

        // Act
        val returnedObject = usersRetriever.verifyCredentials(list, incorrectPhoneNumber, correctPassword)

        // Assert
        assertThat(returnedObject).usingRecursiveComparison().isEqualTo(incorrectUserWrapper)
    }
}