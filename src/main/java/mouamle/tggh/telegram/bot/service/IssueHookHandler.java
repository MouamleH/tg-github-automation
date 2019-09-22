package mouamle.tggh.telegram.bot.service;

import mouamle.tggh.github.web.model.IssueEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public final class IssueHookHandler {

    private final Map<String, Consumer<IssueEvent>> handlersRegistry = new HashMap<>();

    public void registerHandler(String action, Consumer<IssueEvent> callback) {
        handlersRegistry.put(action, callback);
    }

    public void processEvent(IssueEvent event) {
        Consumer<IssueEvent> consumer = handlersRegistry.get(event.getAction());
        if (consumer != null) {
            consumer.accept(event);
        }
    }

}
