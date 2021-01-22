package com.example.sharyan.ui
import com.example.sharyan.data.RequestsFiltersContainer

interface FilterHolder{
    fun submitFilters(requestsFiltersContainer: RequestsFiltersContainer?) {
    }
}