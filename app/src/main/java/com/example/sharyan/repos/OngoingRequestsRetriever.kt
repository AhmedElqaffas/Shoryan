package com.example.sharyan.repos

import com.example.sharyan.data.DonationRequest

object OngoingRequestsRetriever {


    fun getRequestsFromNetwork(): List<DonationRequest>{

        return mutableListOf(
            DonationRequest("AB", "عبد الغفور البرعي", "مدينة نصر"),
            DonationRequest("B+", "مدحت قلابيظو", "المقطم"),
            DonationRequest("B", "الفارس الشجاع", "المقطم"),
            DonationRequest("A+", "وردة البستان", "مصر الجديدة"),
            DonationRequest("O-", "صديق الفلاح", "6 اكتوبر"),
            DonationRequest("B+", "الفلاح", "المعادي"),
        )

    }
}