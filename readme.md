# Video Sharing Social App

Use Firebase Authentication to let users log in with Firebase account using their email addresses and passwords.

Upload video posts on the server from device gallery or camera.

Scroll users posts list, post comments, tap likess and share links with friends.

Watch videos after click on post or download them by tapping on the link.

UI tests with Espresso.

![alt tag](screenshots/socialapp_screenshot.png)



If you want to use your own account, in order to allow login to your app, allow authentication via email and password in tab Authentication/Sign-in method in your project settings (https://console.firebase.google.com/project/YOUR_PROJECT/authentication/providers)
Also, check Database rules in project settings (https://console.firebase.google.com/project/YOUR_PROJECT/database/YOUR_PROJECT/rules). 
My project uses below configs 
{ 
"rules": { 
".read": "auth != null", 
".write": "auth != null" 
} 
} 
