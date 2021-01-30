package com.example.sharyan.data

data class CreateNewRequestQuery(val bloodType : String,
                                 val numberOfBagsRequried : Int,
                                 val urgent : Boolean,
                                 val requestBy : String,
                                 val donationLocation : String)
