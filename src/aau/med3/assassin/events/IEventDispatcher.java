package aau.med3.assassin.events;

public interface IEventDispatcher {
	void addEventListener(String name, EventListener listener);
	void dispatchEvent(String name, Object data);
	void removeEventListeners(String name);
	void removeEventListener(String name, EventListener listener);
	Boolean hasEventListener(String name, EventListener listener);
	void removeAllEventListeners();
}
