package mouamle.tggh.common.handler;

import mouamle.tggh.service.github.web.model.IssueEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A handler to process open/close/reopen actions on the repo
 */
@Component
public final class IssueEventHandler {

    private final Map<String, List<Consumer<IssueEvent>>> handlersRegistry = new HashMap<>();

    public void registerHandler(String action, Consumer<IssueEvent> callback) {
        List<Consumer<IssueEvent>> consumers = handlersRegistry.get(action);
        if (consumers == null) {
            consumers = new ArrayList<>();
        }
        consumers.add(callback);

        handlersRegistry.put(action, consumers);
    }

    public void processEvent(IssueEvent event) {
        List<Consumer<IssueEvent>> consumers = handlersRegistry.get(event.getAction());
        if (consumers != null) {
            consumers.forEach(consumer -> consumer.accept(event));
        }
    }

}
