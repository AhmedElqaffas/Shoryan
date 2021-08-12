# Shoryan
Aiming to facilitate the process of finding blood donors in Egypt, Shoryan allows users to make blood donation requests so that other users donate to them. Shoryan also allows blood banks across Egypt to make blood donation requests if they are in need for blood supply. Shoryan's main goal is to save the lives of patients who desperately need blood donations to survive.

## Features
Shoryan allows users to request or fulfil blood donations. It keeps the users engaged by notifying them of nearby requests and also by providing a rewards system for the donors to redeem stuff from various stores.

### Request Donation
Users can request blood donations by entering the desired blood type, number of blood units, and the blood bank where the donation is needed.




The requester can then track their request status, or they can simply close the application and wait for a notification to arrive whenever a donor accepts the request.


### Fulfil Donation
Users can view and accept donation requests. Donating rewards donors with points that can be later used to redeem rewards. Users can donate by clicking on any donation request in the home screen.



After viewing the request details, the user can choose to start the donation process by clicking on the "donate" button, signaling that they are going to donate to this request.


After donating at the blood bank, the donor can click on "confirm donation" to indicate that they have completed the donation, and that the blood bag can now be received from the blood bank.



### Redeem Rewards
We have laid the basis for the rewards system, however, there no currently no sponsors for our system, so there are no actual rewards that can be redeemed by the donors. Below images contain dummy rewards.



Users can choose a reward to view its details, and redeem it if they want, by selecting the nearest store branch from which they want to receive the reward.


An sms containing a verification code is sent to the store branch mobile, the user should ask for that code when they arrive at the branch to complete the redemption.


Another sms is sent to the branch, confirming the donation. The below screen is shown to the user.





## Android Application Technical Details
This app was developed natively using android studio, it works on devices having android OS 5 (Lollipop) and above. In this project, we have used [MVVM](https://developer.android.com/jetpack/guide) architecture, where the model is responsible for communicating with the backend Restful API.
We have used below android technologies/libraries:
* Jetpack Compose - used to design some of the screens
* Jetpack Navigation Component - for managing the navigation between screens
* Livedata and Couroutine Flow - for managing app state and loading data from the backend asynchronously
* Retrofit - for communicating with the restful API using HTTP protocols
* Google Maps and Places API - used to determine the user's location during registration
* Firebase Cloud Messaging (FCM) - for sending push notifications to the users
* Hilt - facilitates applying dependancy injection to our components
