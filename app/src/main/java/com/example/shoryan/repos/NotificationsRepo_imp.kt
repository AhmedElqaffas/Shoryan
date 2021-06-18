package com.example.shoryan.repos

import com.example.shoryan.data.DonationNotification
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.data.DonationRequester
import com.example.shoryan.networking.RetrofitBloodDonationInterface

class NotificationsRepo_imp(
    private val retrofit: RetrofitBloodDonationInterface
): NotificationsRepo{

//60732298cc69f300049c19da MYID
    private var notificationsList = listOf<DonationNotification>()

    override suspend fun getNotifications(): List<DonationNotification>{
        val request = DonationRequest("606dcdc70ad4440004913ba9",
        DonationRequester("60672c71f3d06b000474ea04"),
        )

        val myRequest = DonationRequest("60cb48f5636d4c00047b4028",
            DonationRequester("60732298cc69f300049c19da"),
        )
        notificationsList = listOf(
            DonationNotification("1","طلب جديد",
                "بنك الدم المتواجد بالقرب منك به نقص، نحن نحتاج اليك", System.currentTimeMillis(), request),
            DonationNotification("2","طلب جديد",
                "بنك الدم المتواجد بالقرب منك به نقص، نحن نحتاج اليك", System.currentTimeMillis() - (1000*60*60), myRequest),
            DonationNotification("3", "طلب جديد",
                "بنك الدم المتواجد بالقرب منك به نقص، نحن نحتاج اليك", System.currentTimeMillis(), request),
            DonationNotification("4", "طلب جديد",
                "بنك الدم المتواجد بالقرب منك به نقص، نحن نحتاج اليك", System.currentTimeMillis(), request),
        )
        return notificationsList
    }
}