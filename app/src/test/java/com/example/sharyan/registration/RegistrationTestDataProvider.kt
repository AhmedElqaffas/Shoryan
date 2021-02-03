package com.example.sharyan.registration

import org.testng.annotations.DataProvider
import java.lang.reflect.Method

class RegistrationTestDataProvider {

    companion object{

        @DataProvider(name = "validNumbers", parallel = true)
        @JvmStatic
        fun validNumbers(): MutableIterator<Array<String>>{
            val testData: ArrayList<Array<String>> = arrayListOf()
            testData.add(arrayOf("01012345678"))
            testData.add(arrayOf("01000000000"))
            return testData.iterator()
        }

        @DataProvider(name = "invalidNumbers", parallel = true)
        @JvmStatic
        fun invalidNumbers(method: Method): MutableIterator<Array<String>>{
            val testData: ArrayList<Array<String>> = arrayListOf()
            when (method.name) {
                "isValidMobilePhoneEntered() WITH number having more than 11 digits RETURN false" -> {
                    testData.add(arrayOf("010123456780"))
                    testData.add(arrayOf("0100000000000"))
                }
                "isValidMobilePhoneEntered() WITH number having less than 11 digits RETURN false" -> {
                    testData.add(arrayOf(""))
                    testData.add(arrayOf(" "))
                    testData.add(arrayOf("01"))
                    testData.add(arrayOf("0100000008"))
                }
                "isValidMobilePhoneEntered() WITH non-leading 01 RETURN false" -> {
                    testData.add(arrayOf("1111111111"))
                    testData.add(arrayOf("11111111111"))
                    testData.add(arrayOf("00000000000"))
                    testData.add(arrayOf("02100000000"))
                }
                "isValidMobilePhoneEntered() WITH string containing non digits RETURN false" -> {
                    testData.add(arrayOf("01-97856966"))
                    testData.add(arrayOf("01_11111111"))
                    testData.add(arrayOf("01000_000000"))
                    testData.add(arrayOf("_0100000000"))
                    testData.add(arrayOf("_01000000000"))
                    testData.add(arrayOf(".0100000000"))
                    testData.add(arrayOf("01x97856966"))
                    testData.add(arrayOf("01y11111111"))
                    testData.add(arrayOf("01 11111111"))
                    testData.add(arrayOf("01 111111111"))
                }
            }
            return testData.iterator()
        }

        @DataProvider(name = "validNames", parallel = true)
        @JvmStatic
        fun validNames(): MutableIterator<Array<String>>{
            val testData: ArrayList<Array<String>> = arrayListOf()
            testData.add(arrayOf("عمرو"))
            testData.add(arrayOf("شاةثي"))
            testData.add(arrayOf("دجحخهععغفقثصضشسيبلاألأآلآإلإاتنمكطظزوةىلارؤءئذ"))
            testData.add(arrayOf("عبدالحق"))
            testData.add(arrayOf("احمـــد"))
            testData.add(arrayOf("ahmed"))
            testData.add(arrayOf("Omar"))
            testData.add(arrayOf("ZiAD"))
            testData.add(arrayOf("joe"))
            return testData.iterator()
        }

        @DataProvider(name = "invalidNames", parallel = true)
        @JvmStatic
        fun invalidNames(method: Method): MutableIterator<Array<String>>{
            val testData: ArrayList<Array<String>> = arrayListOf()
            when (method.name) {
                "isValidNameEntered() WITH name having space in middle RETURN false" -> {
                    testData.add(arrayOf("                "))
                    testData.add(arrayOf("مصطفى شعبان"))
                    testData.add(arrayOf("yasser rizk"))
                    testData.add(arrayOf("yasser     rizk"))
                    testData.add(arrayOf(" osame"))
                    testData.add(arrayOf(" yasser     rizk"))
                }
                "isValidNameEntered() WITH string having special characters RETURN false" -> {
                    testData.add(arrayOf("___"))
                    testData.add(arrayOf("K.I.L.L.E.R"))
                    testData.add(arrayOf("oma>r"))
                    testData.add(arrayOf("عليّ"))
                    testData.add(arrayOf("F***"))
                }
                "isValidNameEntered() WITH name having digits RETURN false" -> {
                    testData.add(arrayOf("9ahmed"))
                    testData.add(arrayOf("ahmed7"))
                    testData.add(arrayOf("5عمرو1"))
                    testData.add(arrayOf("م999صطفى"))
                }
                "isValidNameEntered() WITH arabic english mix RETURN false" -> {
                    testData.add(arrayOf("Drاحمد"))
                    testData.add(arrayOf("رررkتاحمد"))
                }
                "isValidNameEntered() WITH 1 letter name RETURN false" -> {
                    testData.add(arrayOf("a"))
                    testData.add(arrayOf(""))
                    testData.add(arrayOf(" "))
                    testData.add(arrayOf("ا"))
                }
            }
            return testData.iterator()
        }

        @DataProvider(name = "validPasswords", parallel = true)
        @JvmStatic
        fun validPasswords(): MutableIterator<Array<String>>{
            val testData: ArrayList<Array<String>> = arrayListOf()
            testData.add(arrayOf("عمرو"))
            testData.add(arrayOf("youssef"))
            return testData.iterator()
        }

        @DataProvider(name = "invalidPasswords_spaces", parallel = true)
        @JvmStatic
        fun invalidPasswordsSpaces(): MutableIterator<Array<String>>{
            val testData: ArrayList<Array<String>> = arrayListOf()
            testData.add(arrayOf(""))
            testData.add(arrayOf(" "))
            testData.add(arrayOf(" ahmed"))
            testData.add(arrayOf("ahmed "))
            testData.add(arrayOf("os ama"))
            testData.add(arrayOf(" am   r "))
            return testData.iterator()
        }
    }
}