/*! \mainpage Accela SDK for Android
 *
 * \section intro_sec Introduction
 *
 *
 *  Accela SDK for Android allows you to add Accela Automation functionality to your applications designed for phones or tablets running Android operating system. The SDK includes a Java API that leverages functionality provided by Accela Cloud services through the REST interface. The API primarily provides record and inspection components. The record component accesses record information and allows for simple create of records. The inspection component allows access to inspection data with functions to schedule, reschedule, and cancel.
 *
 * The API is available in a single AccelaSDK JAR library. Classes and functions defined in this library begin with the prefix "AM". This prefix acts as a namespace and prevents naming conflicts with classes defined in your applications or other frameworks you use.
 *
 * \section install_sec Setting Up Your Android Project
 *
 * When creating an Android project, you are recommended to select API level 9 (Android 2.3.1) or higher as the minimum SDK version. Check the available Android SDKs and set the project's build target version accordingly.
 *
 * The Accela SDK JAR library (AccelaSDK.jar) should be copied to the "libs" folder under the Android project's home directory.
 * In addition, the Android Support Library (android-support-v4.jar) should also be put into the "libs" folder because the Accela SDK refers to the class <i>android.support.v4.content.LocalBroadcastManager</i>. 
* For how to install the Android Support Library, please refer to the instructions listed in the webpage <i>http://developer.android.com/training/basics/fragments/support-lib.html</i>.
 *
 * The permission "android.permission.INTERNET" must be declared in the Android project's configuration file <i>AndroidManifest.xml </i>. 
 * The AuthorizationActivity activity must also be declared because it is required in user authorization. Note the value of android:scheme should be assigned to the urlSchema property in the AccelaMobile instance you initialized.
 * Here is the declaration content of permission and activity:
 \verbatim
<uses-permission android:name="android.permission.INTERNET" />
 ...
<activity android:name="com.accela.mobile.AuthorizationActivity" android:windowSoftInputMode="stateHidden" android:launchMode="singleInstance" >
  <intent-filter>
    <action android:name="android.intent.action.VIEW"></action>
    <category android:name="android.intent.category.DEFAULT"></category>
    <category android:name="android.intent.category.BROWSABLE"></category>
    <data android:scheme="aminspectionviewer" android:host="authorize"></data> 			
  </intent-filter> 
</activity> \endverbatim
 *
 */
