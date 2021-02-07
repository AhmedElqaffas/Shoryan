package com.example.shoryan.repos

import com.example.shoryan.data.DonationNotification

object NotificationsRepo{


    private var notificationsList = listOf<DonationNotification>()

    fun getNotifications(): List<DonationNotification>{
        if(notificationsList.isNullOrEmpty()){
            notificationsList = listOf(
                DonationNotification("طلب جديد",
                    "بنك الدم المتواجد بالقرب منك به نقص، نحن نحتاج اليك", "3 دقائق" ,false),
                DonationNotification("طلب جديد",
                    "بنك الدم المتواجد بالقرب منك به نقص، نحن نحتاج اليك", "13 ايام" ,false),
                DonationNotification("طلب جديد",
                    "بنك الدم المتواجد بالقرب منك به نقص، نحن نحتاج اليك", "3 ساعات " ,true),
                DonationNotification("طلب جديد",
                    "بنك الدم المتواجد بالقرب منك به نقص، نحن نحتاج اليك", "7 اسابيع" ,true),
            )
        }
        return notificationsList
    }
}