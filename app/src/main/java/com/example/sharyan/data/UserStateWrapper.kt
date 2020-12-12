package com.example.sharyan.data

/**
 * This class is used to wrap User objects and add state to them.
 * The state is a way to pass messages to the activity instead of Exceptions, as Exceptions
 * can't be caught from observable.
 */
data class UserStateWrapper(val user: User?, val error: String?)
