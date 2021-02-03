package com.example.sharyan.registration

import com.example.sharyan.ui.RegistrationViewModel
import org.testng.AssertJUnit.assertEquals
import org.testng.annotations.Test

class RegistrationViewModelTest {

    private val registrationViewModel = RegistrationViewModel()

    @Test(dataProvider = "validNumbers", dataProviderClass =RegistrationTestDataProvider::class)
    fun `isValidMobilePhoneEntered() WITH correct number RETURN true`(phoneNumber: String) {
        assertEquals(true, registrationViewModel.isValidMobilePhoneEntered(phoneNumber))
    }

    @Test(dataProvider = "invalidNumbers", dataProviderClass =RegistrationTestDataProvider::class)
    fun `isValidMobilePhoneEntered() WITH number having more than 11 digits RETURN false`(phoneNumber: String) {
        assertEquals(false, registrationViewModel.isValidMobilePhoneEntered(phoneNumber))
    }

    @Test(dataProvider = "invalidNumbers", dataProviderClass =RegistrationTestDataProvider::class)
    fun `isValidMobilePhoneEntered() WITH number having less than 11 digits RETURN false`(phoneNumber: String) {
        assertEquals(false, registrationViewModel.isValidMobilePhoneEntered(phoneNumber))
    }

    @Test(dataProvider = "invalidNumbers", dataProviderClass =RegistrationTestDataProvider::class)
    fun `isValidMobilePhoneEntered() WITH non-leading 01 RETURN false`(phoneNumber: String) {
        assertEquals(false, registrationViewModel.isValidMobilePhoneEntered(phoneNumber))
    }

    @Test(dataProvider = "invalidNumbers", dataProviderClass =RegistrationTestDataProvider::class)
    fun `isValidMobilePhoneEntered() WITH string containing non digits RETURN false`(phoneNumber: String) {
        assertEquals(false, registrationViewModel.isValidMobilePhoneEntered(phoneNumber))
    }

    @Test(dataProvider = "validNames", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidNameEntered() WITH name having no spaces, special chars, or digits RETURN true`(name: String) {
        assertEquals(true, registrationViewModel.isValidNameEntered(name))
    }

    @Test(dataProvider = "invalidNames", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidNameEntered() WITH name having space RETURN false`(name: String) {
        assertEquals(false, registrationViewModel.isValidNameEntered(name))
    }

    @Test(dataProvider = "invalidNames", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidNameEntered() WITH string having special characters RETURN false`(name: String) {
        assertEquals(false, registrationViewModel.isValidNameEntered(name))
    }

    @Test(dataProvider = "invalidNames", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidNameEntered() WITH name having digits RETURN false`(name: String) {
        assertEquals(false, registrationViewModel.isValidNameEntered(name))
    }

    @Test(dataProvider = "invalidNames", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidNameEntered() WITH arabic english mix RETURN false`(name: String) {
        assertEquals(false, registrationViewModel.isValidNameEntered(name))
    }

    @Test(dataProvider = "invalidNames", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidNameEntered() WITH 1 letter name RETURN false`(name: String) {
        assertEquals(false, registrationViewModel.isValidNameEntered(name))
    }

    @Test(dataProvider = "validPasswords", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidPasswordEntered() WITH valid password RETURN true`(password: String){
        assertEquals(true, registrationViewModel.isValidPasswordEntered(password))
    }

    @Test(dataProvider = "invalidPasswords_spaces", dataProviderClass = RegistrationTestDataProvider::class)
    fun `isValidPasswordEntered() WITH password containing spaces RETURN false`(password: String){
        assertEquals(false, registrationViewModel.isValidPasswordEntered(password))
    }
}