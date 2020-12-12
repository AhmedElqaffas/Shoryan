package com.example.sharyan.repos


import android.util.Log
import com.example.sharyan.data.User
import com.example.sharyan.data.UserStateWrapper
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlin.Exception

object UsersRetriever {

    var  usersList = listOf<User>()

    suspend fun checkCredentials(bloodDonationAPI: RetrofitBloodDonationInterface,
    phoneNumber: String, password: String): UserStateWrapper{

        CoroutineScope(Dispatchers.Default).async {
            usersList = getUsers(bloodDonationAPI)
        }.await()

        return verifyCredentials(usersList, phoneNumber, password)
    }

     suspend fun getUsers(bloodDonationAPI: RetrofitBloodDonationInterface): List<User>{

        if(usersList.isNullOrEmpty()){
            return try{
                usersList = bloodDonationAPI.getAllRegisteredUsers()
                usersList
            } catch(e: Exception){
                Log.e("UserRequestsAPICall","Couldn't get requests" + e.message)
                listOf()
            }
        }
        return usersList
    }

    fun verifyCredentials(usersList: List<User>, phoneNumber: String, password: String): UserStateWrapper{
        for(user in usersList){
            // phoneNumber is 1097049699  password is pass1
            if(("0"+user.phoneNumber == phoneNumber) and (user.password == password)){
                return UserStateWrapper(user,null)
            }
        }

        return UserStateWrapper(null, "خطأ في رقم الهاتف او كلمة السر")
    }
}
