# Overview

This project is a simple Contacts Add app. This app will talk with the devices personal contacts app, and allow the user to input a name, up to 2 phone numbers, an email, and
a photo. The user can then click the floating action button with a check mark to save this contact into the phones actual contact list.

While this app may seem redundant as all phones have a contacts app with their own add contact function, this served as a good exercise in creating functional apps, and learning
to use device storage. It also was practice in interfacing with existing android apps. 

[Software Demo Video](https://youtu.be/O3Ri7cc_2UA)

# Development Environment
For this project I used Android Studio as my IDE of choice. It offered the easiest integration with Android devices, and also includes an emulator to further aid in testing.
Android studio's use of XML is also very easy to understand, and allows us to view changes in real time.

To program this app I used Kotlin. No extra libraries or plug ins were needed except the default plug ins, but the content provider operation function within Kotlin is what does 
the bulk of the heavy lifting in this app. It allows the app to save attributes of the contact, and then send them to the mobiles devices contact app.

# Useful Websites

This thread on StackOverflow is a useful thread on how to use Kotlin's Content Provider Operation.
* [StackOverflow](https://stackoverflow.com/questions/4744187/how-to-add-new-contacts-in-android)

Creating gallery intents or new intents was previously done with startActivityForResult(). However, this has depreciated. This website was helping in finding a solution.
* [MongoDB](https://www.mongodb.com/developer/languages/kotlin/realm-startactivityforresult-registerforactivityresult-deprecated-android-kotlin/)

# Future Work

While this app is a good use of device storage, I would like to create a standalone contacts app that has it's own list and keeps track of its own contacts.
There are also a number of things I would like to improve on, such as:

* The contact photo is not centered correctly. I struggled with figuring out how to use the image bitstream generation, I would like to improve this functionality.
* Create a standalone version with the apps own contact list.
* Add a splash screen and do general GUI improvements.
