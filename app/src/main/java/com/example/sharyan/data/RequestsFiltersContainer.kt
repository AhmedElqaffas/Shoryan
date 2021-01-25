package com.example.sharyan.data

import java.io.Serializable

data class RequestsFiltersContainer(val bloodType: Set<BloodType>): Serializable
