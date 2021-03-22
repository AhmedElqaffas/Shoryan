package com.example.shoryan.registration

import com.example.shoryan.InputValidator
import org.testng.AssertJUnit.assertEquals
import org.testng.annotations.Test

class RegistrationInputValidationTest {

    private val inputValidator = InputValidator

    @Test(dataProvider = "validNumbers", dataProviderClass =RegistrationTestDataProvider::class)
    fun `isValidMobilePhoneEntered() WITH correct number RETURN true`(phoneNumber: String) {
        assertEquals(true, inputValidator.isValidMobilePhoneEntered(phoneNumber))
    }

    @Test(dataProvider = "invalidNumbers", dataProviderClass =RegistrationTestDataProvider::class)
    fun `isValidMobilePhoneEntered() WITH non-Egyptian prefix RETURN false`(phoneNumber: String) {
        assertEquals(false, inputValidator.isValidMobilePhoneEntered(phoneNumber))
    }

    @Test(dataProvider = "invalidNumbers", dataProviderClass =RegistrationTestDataProvider::class)
    fun `isValidMobilePhoneEntered() WITH string containing non digits RETURN false`(phoneNumber: String) {
        assertEquals(false, inputValidator.isValidMobilePhoneEntered(phoneNumber))
    }

    @Test(dataProvider = "validNames", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidNameEntered() WITH name having no multiple spaces, special chars, or digits RETURN true`(name: String) {
        assertEquals(true, inputValidator.isValidNameEntered(name))
    }

    @Test(dataProvider = "invalidNames", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidNameEntered() WITH name having multiple space RETURN false`(name: String) {
        assertEquals(false, inputValidator.isValidNameEntered(name))
    }

    @Test(dataProvider = "invalidNames", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidNameEntered() WITH string having special characters RETURN false`(name: String) {
        assertEquals(false, inputValidator.isValidNameEntered(name))
    }

    @Test(dataProvider = "invalidNames", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidNameEntered() WITH name having digits RETURN false`(name: String) {
        assertEquals(false, inputValidator.isValidNameEntered(name))
    }

    @Test(dataProvider = "invalidNames", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidNameEntered() WITH arabic english mix RETURN false`(name: String) {
        assertEquals(false, inputValidator.isValidNameEntered(name))
    }

    @Test(dataProvider = "invalidNames", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidNameEntered() WITH 1 letter name RETURN false`(name: String) {
        assertEquals(false, inputValidator.isValidNameEntered(name))
    }

    @Test(dataProvider = "validPasswords", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidPasswordEntered() WITH valid password RETURN true`(password: String){
        assertEquals(true, inputValidator.isValidPasswordEntered(password))
    }

    @Test(dataProvider = "invalidPasswords", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidPasswordEntered() WITH invalid password RETURN false`(password: String){
        assertEquals(false, inputValidator.isValidPasswordEntered(password))
    }
}