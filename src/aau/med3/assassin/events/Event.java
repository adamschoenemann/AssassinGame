package aau.med3.assassin.events;

public class Event {
	
	// Event constants
	public final static String 	SUCCESS = "success",
									FAILURE = "failure",
									INTERRUPTED = "interrupted",
									STATE_CHANGED = "stateChanged";
	
	
	
	public String name;
	public EventDispatcher target;
	public Object data;
}
