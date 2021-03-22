package com.example.shoryan.interfaces
import com.example.shoryan.data.RequestsFiltersContainer
import java.io.Serializable

interface FilterHolder: Serializable{
    fun submitFilters(requestsFiltersContainer: RequestsFiltersContainer?) {
    }
}