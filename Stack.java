import java.util.LinkedList;
import java.util.List;

/**
 * A simple stack implementation.
 * 
 * @author zpf.073@gmail.com
 *
 */
public class Stack<T> {

	private List<T> values;

	public Stack() {
		values = new LinkedList<T>();
	}

	public T pop() {
		if (!values.isEmpty()) {
			return values.remove(values.size() - 1);
		}
		return null;
	}

	public void push(T value) {
		values.add(value);
	}
}
