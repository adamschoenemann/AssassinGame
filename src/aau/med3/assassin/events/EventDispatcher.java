package aau.med3.assassin.events;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;


public class EventDispatcher implements IEventDispatcher {
	private static final String TAG = "EVENT_DISPATCHER";
	Map<String, List<EventListener>> listenerMap = new HashMap<String, List<EventListener>>();
	
	public void addEventListener(String name, EventListener listener){
		
		if(listenerMap.containsKey(name)){
			 listenerMap.get(name).add(listener);
		}
		else {
			 List<EventListener> listeners = new LinkedList<EventListener>();
			 listeners.add(listener);
			 listenerMap.put(name, listeners);
		}
//		Log.d(TAG, "addEventListener called");
	}
	
	public void dispatchEvent(String name, Object data){
		
		if(listenerMap.containsKey(name) == false){
//			Log.d(TAG, "dispatchEvent called but not event with name " + name + " found");
			return;
		}
		
		Event event = new Event();
		event.name = name;
		event.target = this;
		event.data = data;
		
		List<EventListener> listeners = listenerMap.get(name);
		for(EventListener listener : listeners){
//			Log.d(TAG, "dispatchEvent called with event: " + name);
			listener.handle(event);
		}
		
	}
	
	public void removeEventListeners(String name){
		listenerMap.remove(name);
//		Log.d(TAG, "removeEventListeners called");
	}
	
	public void removeEventListener(String name, EventListener listener){
		List<EventListener> listeners = listenerMap.get(name);
		if(listeners == null) return;
		
		
		for(EventListener l : listeners){
			if(l.getClass().equals(listener.getClass())){
//				Log.d(TAG, listener.getClass().toString() + " removed at index " + index);

				if(listeners.remove(l) == true){
					if(listeners.size() == 0){
						listenerMap.remove("name");
					}
				} else {
					Log.d(TAG, "Event List not modified!");
				}
				return;
			}
			
		}
//		Log.d(TAG, "Listeners remaing: " + printEventListeners(name));
//		Log.d(TAG, listener.getClass().toString() + " not found. None removed.");
//		Log.d(TAG, "removeEventListener called");
	}
	
	public Boolean hasEventListener(String name, EventListener listener){
		List<EventListener> listeners = listenerMap.get(name);
		if(listeners == null) return false;
		
		for(EventListener l : listeners){
			if(l.getClass().equals(listener.getClass())){
				return true;
			}
			
		}
		return false;
	}
	
	public String printEventListeners(String name){
		String ret = "";
		List<EventListener> listeners = listenerMap.get(name);
		for(EventListener l : listeners){
			ret += l.toString() + "\n";
		}
		return ret;
		
	}
	
	public void removeAllEventListeners(){
		listenerMap.clear();
	}
	
}
