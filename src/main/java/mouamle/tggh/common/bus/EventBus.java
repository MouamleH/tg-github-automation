package mouamle.tggh.common.bus;

import java.util.function.Consumer;

/**
 * Pub/Sub data structure definition
 * @param <E> The element
 */
public interface EventBus<E> {

    /**
     * Used to publish the element to all subscribers
     * @param element the element to be published
     */
    void publish(E element);

    /**
     * Used for registering a subscriber
     * @param callback the {@link Consumer} that will get the published element
     */
    void subscribe(Consumer<E> callback);

}
