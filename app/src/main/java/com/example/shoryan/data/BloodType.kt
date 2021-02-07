package com.example.shoryan.data

import com.google.gson.annotations.SerializedName

enum class BloodType(val bloodType: String){
    @SerializedName("A+")
    APositive("A+"){
        override fun getCompatibleTypes(): Set<BloodType> {
            return setOf(APositive, ABPositive)
        }
    },

    @SerializedName("A-")
    ANegative("A-"){
        override fun getCompatibleTypes(): Set<BloodType> {
            return setOf(APositive, ABPositive, ANegative, ABNegative)
        }
    },

    @SerializedName("B+")
    BPositive("B+"){
        override fun getCompatibleTypes(): Set<BloodType> {
            return setOf(BPositive, ABPositive)
        }
    },

    @SerializedName("B-")
    BNegative("B-"){
        override fun getCompatibleTypes(): Set<BloodType> {
            return setOf(BNegative, BPositive, ABPositive, ABNegative)
        }
    },

    @SerializedName("AB+")
    ABPositive("AB+"){
        override fun getCompatibleTypes(): Set<BloodType> {
            return setOf(ABPositive)
        }
    },

    @SerializedName("AB-")
    ABNegative("AB-"){
        override fun getCompatibleTypes(): Set<BloodType> {
            return setOf(ABPositive, ABNegative)
        }
    },

    @SerializedName("O+")
    OPositive("O+"){
        override fun getCompatibleTypes(): Set<BloodType> {
            return setOf(APositive, BPositive, ABPositive, OPositive)
        }
    },

    @SerializedName("O-")
    ONegative("O-"){
        override fun getCompatibleTypes(): Set<BloodType> {
            return setOf(APositive, ANegative, BNegative, BPositive, ABPositive, ABNegative, ONegative, OPositive)
        }
    };

    abstract fun getCompatibleTypes(): Set<BloodType>

    companion object {
        fun fromString(bloodType: String) = values().first { it.bloodType == bloodType }
    }
}