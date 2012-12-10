package aau.med3.assassin.events;

public interface EventHandler<T> {
	public void onEvent(T data);
	
}
