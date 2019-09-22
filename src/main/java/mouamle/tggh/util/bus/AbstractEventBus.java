package mouamle.tggh.util.bus;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract implementation for the {@link EventBus}
 *
 * @param <E> the element
 */
public class AbstractEventBus<E> implements EventBus<E> {

    private final List<Consumer<E>> subscribers = new LinkedList<>();

    @Override
    public void publish(E element) {
        subscribers.forEach(subscriber -> subscriber.accept(element));
    }

    @Override
    public void subscribe(Consumer<E> callback) {
        subscribers.add(callback);
    }

}
