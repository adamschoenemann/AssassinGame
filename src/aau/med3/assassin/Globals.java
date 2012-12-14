package aau.med3.assassin;

import aau.med3.assassin.events.EventDispatcher;

public class Globals {
	public final static String SERVER_LOCATION = "10.0.2.2/android_test/";
	public final static String PREF_FILENAME = "AssassinPrefs";
	public final static String DEBUG = "DEBUG";
	public final static String SENDER_ID = "547412144821";
	public final static String ACTION_REFRESH = "aau.med3.assassin.ACTION_REFRESH";
	public final static String ACTION_LOGGED_IN = "aau.med3.assassin.ACTION_LOGGED_IN";
	public final static String ACTION_LOGGED_OUT = "aau.med3.assassin.ACTION_LOGGED_OUT";
	public static User user;
	public static AssassinService assassinService;
	public static EventDispatcher events = new EventDispatcher();
}
