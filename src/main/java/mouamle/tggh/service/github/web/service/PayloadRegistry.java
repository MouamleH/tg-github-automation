package mouamle.tggh.service.github.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import mouamle.tggh.service.github.web.model.IssueEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Serialization handler for github push payload
 */
@Component
@SuppressWarnings("unchecked")
public class PayloadRegistry {

    private final ObjectMapper mapper;
    private static final Map<String, Class> eventsMap = new HashMap<>();

    static {
        eventsMap.put("issues", IssueEvent.class);
    }

    public PayloadRegistry(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> void process(String event, String payload, Consumer<T> consumer) throws IOException {
        Object parsed = mapper.readValue(payload, eventsMap.getOrDefault(event, Object.class));
        if (parsed != null) {
            consumer.accept((T) parsed);
        }
    }

}
