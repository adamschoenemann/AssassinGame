package aau.med3.assassin.events;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.util.Log;


public class EventDispatcher implements IEventDispatcher {
	private static final String TAG = "EVENT_DISPATCHER";
	Map<String, List<EventListener>> events = new HashMap<String, List<EventListener>>();
	
	public void addEventListener(String name, EventListener listener){
		
		if(events.containsKey(name)){
			 events.get(name).add(listener);
		}
		else {
			 List<EventListener> listeners = new LinkedList<EventListener>();
			 listeners.add(listener);
			 events.put(name, listeners);
		}
//		Log.d(TAG, "addEventListener called");
	}
	
	public void dispatchEvent(String name, Object data){
		
		if(events.containsKey(name) == false){
//			Log.d(TAG, "dispatchEvent called but not event with name " + name + " found");
			return;
		}
		
		Event event = new Event();
		event.name = name;
		event.target = this;
		event.data = data;
		
		List<EventListener> listeners = events.get(name);
		for(EventListener listener : listeners){
//			Log.d(TAG, "dispatchEvent called with event: " + name);
			listener.handle(event);
		}
		
	}
	
	public void removeEventListeners(String name){
		events.remove(name);
//		Log.d(TAG, "removeEventListeners called");
	}
	
	public void removeEventListener(String name, EventListener listenerAddress){
		List<EventListener> listeners = events.get("name");
		if(listeners == null) return;
		
		Integer index = 0;
		for(EventListener listener : listeners){
			if(listener == listenerAddress){
				listeners.remove(index);
				break;
			}
			index++;
		}
//		Log.d(TAG, "removeEventListener called");
	}
	
}
