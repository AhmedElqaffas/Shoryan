package com.example.sharyan.ui
import com.example.sharyan.data.RequestsFiltersContainer
import java.io.Serializable

interface FilterHolder: Serializable{
    fun submitFilters(requestsFiltersContainer: RequestsFiltersContainer?) {
    }
}