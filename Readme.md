# Husky Messenger

Developed by:<br>
Jon Anderson<br>
Marshall Freed<br>
Karan Kurbur<br>
Mahad Fahiye<br>
<br>
Implemented:<br>
<br>
Register<br>
	Required info:<br>
		First and last name<br>
		Email (verified)<br>
		Nickname (displayed in app)<br>
		Password<br>
	Client side checks for validity<br>
		Password requires 5+ characters with 1+ uppercase and 1+ number<br>
	On registration attempt, takes the user to verification page to enter code from email<br>
<br>
Login<br>
	Uses username<br>
	Client and server side checks<br>
		Client side checks for no empty fields and password requirements<br>
		Server side makes sure the login credentials exist in the database<br>
	Option to stay logged in stored in shared preferences<br>
	Upon successful login, takes user to home/landing page<br>
<br>
Connections<br>
	View existing connections<br>
	View connections sent by you<br>
	View connections sent to you<br>
	Searching for new connections yields results<br>
		Searches by email, username, first or last name<br>
	Remove existing connections<br>
	Rescind connections requests<br>
	Accept or deny connection requests<br>
	Start a chat directly from the connections page<br>
<br>
Chat<br>
	Individual or group chat with existing connections<br>
		Start and end a chat<br>
		Send and receive messages<br>
			Messages are stored server side<br>
	Continue an individual chat with an existing connection<br>
	Open a new chat request from an existing connection<br>
	Ability to add or remove members from a chat while inside that chat<br>
	<br>
Notifications<br>
	All notifications appear when:<br>
		App is not in the foreground<br>
		The user is viewing the app on the home page<br>
		The user is viewing the app outside the context of the notification (limited)<br>
	Notifications for the following reasons<br>
		New connection request<br>
		New messages from an existing conversation<br>
	Notifications when the app is not visible only appear when logged in<br>
	Notifications display:<br>
		Via status bar when app is not in the foreground<br>
			Selecting the notifications opens the correct state<br>
			Status bar/notification drawer shows sender and notification type<br>
		Via a notification bar when the app is in the foreground<br>
			Bar notifies user where to navigate for notification<br>
<br>
Weather<br>
	Displays the weather forecast for:<br>
		The devices location<br>
	Ability to save any of the above locations to display that weather locations forecast at this time<br>
	Weather display includes:<br>
		Current conditions <br>
		24 hour forecast<br>
		10-day forecast <br>
	<br>
Home/Landing Page<br>
	When a user logs in our home page will display:<br>
		Notification bar displaying new notifications with notification type<br>
		Links to 5 most recent chats loaded dynamically<br>
		Button to navigate to search connections menu<br>
		Custom logo and greeting<br>
	Ability to start new chats from homepage if a user has less than 5 recent chats<br>
		<br>
Look and Feel<br>
	Custom logo on the login page<br>
	Logo as the app icon<br>
	Logo on the navigation drawer<br>
	Themes and styles<br>
		Option to select from 4 themes<br>
			Saved in shared preferences<br>
	
	<br>
Not Yet Implemented:<br>

UI for weather data<br>
Chat is not scaled correctly on all devices<br>
The ability to display weather forecast by selecting location on a map<br>
A location searched for by zip code<br>
