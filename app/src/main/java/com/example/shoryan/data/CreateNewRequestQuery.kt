package com.example.shoryan.data

data class CreateNewRequestQuery(val bloodType : String,
                                 val numberOfBagsRequired : Int,
                                 val urgent : Boolean,
                                 val requestBy : String,
                                 val donationLocation : String)
