package com.example.shoryan

object TimestampToElapsedTime {
    fun convert(timestamp: Long): String{
        val elapsedTimeMS: Long = System.currentTimeMillis() - timestamp

        return when {
            elapsedTimeMS / 1000 < 60 -> {
                "${elapsedTimeMS / 1000} Seconds ago"
            }
            elapsedTimeMS / (1000*60) < 60 -> {
                "${elapsedTimeMS / (1000*60)} Minutes ago"
            }

            elapsedTimeMS / (1000*60*60) < 24 -> {
                "${elapsedTimeMS / (1000*60*60)} Hours ago"
            }

            elapsedTimeMS / (1000*60*60*24) < 30 -> {
                "${elapsedTimeMS / (1000*60*60*24)} Days ago"
            }

            (elapsedTimeMS / (1000*60*60*24)) / 30 < 12 -> {
                "${(elapsedTimeMS / (1000*60*60*24)) / 30} Months ago"
            }

            else -> {
                "${(elapsedTimeMS / (1000*60*60)) / (30*12)} Years ago"
            }
        }
    }
}