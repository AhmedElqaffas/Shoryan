package com.example.shoryan.registration

import org.testng.annotations.DataProvider
import java.lang.reflect.Method

class RegistrationTestDataProvider {

    companion object{

        @DataProvider(name = "validNumbers", parallel = true)
        @JvmStatic
        fun validNumbers(): MutableIterator<Array<String>>{
            val testData: ArrayList<Array<String>> = arrayListOf()
            testData.add(arrayOf("01112345678"))
            testData.add(arrayOf("01000000000"))
            testData.add(arrayOf("01287089347"))
            testData.add(arrayOf("01567789342"))
            return testData.iterator()
        }

        @DataProvider(name = "invalidNumbers", parallel = true)
        @JvmStatic
        fun invalidNumbers(method: Method): MutableIterator<Array<String>>{
            val testData: ArrayList<Array<String>> = arrayListOf()
            when (method.name) {

                "isValidMobilePhoneEntered() WITH non-Egyptian prefix RETURN false" -> {
                    testData.add(arrayOf("1111111111"))
                    testData.add(arrayOf("11111111111"))
                    testData.add(arrayOf("00000000000"))
                    testData.add(arrayOf("02100000000"))
                    testData.add(arrayOf("0100000000"))
                    testData.add(arrayOf("01200000000000000"))
                    testData.add(arrayOf("51097027697"))
                    testData.add(arrayOf("01378650144"))
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
            testData.add(arrayOf("ألأآلآإلإاتةىلارؤءئذ"))
            testData.add(arrayOf("نمكطظزو"))
            testData.add(arrayOf("دجحخهععغفقثصضشسيبلا"))
            testData.add(arrayOf("عبدالحق"))
            testData.add(arrayOf("عبد الحق"))
            testData.add(arrayOf("احمـــد"))
            testData.add(arrayOf("ahmed"))
            testData.add(arrayOf("Omar"))
            testData.add(arrayOf("HODA"))
            testData.add(arrayOf("ZiAD"))
            testData.add(arrayOf("joe"))
            testData.add(arrayOf("مصطفى شعبان"))
            return testData.iterator()
        }

        @DataProvider(name = "invalidNames", parallel = true)
        @JvmStatic
        fun invalidNames(method: Method): MutableIterator<Array<String>>{
            val testData: ArrayList<Array<String>> = arrayListOf()
            when (method.name) {
                "isValidNameEntered() WITH name having multiple space RETURN false" -> {
                    testData.add(arrayOf("                "))
                    testData.add(arrayOf("yasser     rizk"))
                    testData.add(arrayOf(" yasser     rizk"))
                    testData.add(arrayOf("محمد     عصمت"))
                    testData.add(arrayOf("يوسف  شوبير"))
                    testData.add(arrayOf(" شوبير"))
                    testData.add(arrayOf("سيب "))
                    testData.add(arrayOf(" سيب "))
                    testData.add(arrayOf("yasser rizk"))
                    testData.add(arrayOf(" osame"))
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
            testData.add(arrayOf("AnaWadgamed7"))
            testData.add(arrayOf("Killer78"))
            testData.add(arrayOf("Killeer0"))
            testData.add(arrayOf("9NoobMaster"))
            testData.add(arrayOf("9Noob____Master"))
            testData.add(arrayOf("Noob____Master7"))
            testData.add(arrayOf("XXXX**hoho**XXXXX5....?"))
            return testData.iterator()
        }

        @DataProvider(name = "invalidPasswords", parallel = true)
        @JvmStatic
        fun invalidPasswordsSpaces(): MutableIterator<Array<String>>{
            val testData: ArrayList<Array<String>> = arrayListOf()
            testData.add(arrayOf(""))
            testData.add(arrayOf(" "))
            testData.add(arrayOf(" ahmed"))
            testData.add(arrayOf("ahmed "))
            testData.add(arrayOf("os ama"))
            testData.add(arrayOf(" am   r "))
            testData.add(arrayOf("SherifPassword"))
            testData.add(arrayOf("SSVSD3242340....SDC"))
            testData.add(arrayOf("anasmallbas8"))
            testData.add(arrayOf("7777777777777"))
            testData.add(arrayOf("عمرووووووووووووووووووووووووو"))
            testData.add(arrayOf("youssefffffffffffffffffff"))
            testData.add(arrayOf("XXXX****XXXXX5....?"))
            return testData.iterator()
        }
    }
}