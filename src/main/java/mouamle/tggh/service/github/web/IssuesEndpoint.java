package mouamle.tggh.service.github.web;

import mouamle.tggh.service.github.web.model.IssueEvent;
import mouamle.tggh.service.github.web.service.PayloadRegistry;
import mouamle.tggh.common.bus.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class IssuesEndpoint {

    private final EventBus<IssueEvent> bus;
    private final PayloadRegistry payloadRegistry;

    @Autowired
    public IssuesEndpoint(EventBus<IssueEvent> bus, PayloadRegistry payloadRegistry) {
        this.payloadRegistry = payloadRegistry;
        this.bus = bus;
    }

    @PostMapping("hook")
    public void hook(@RequestBody String payload,
                     @RequestHeader("X-Hub-Signature") String signature,
                     @RequestHeader("X-Github-Event") String event) throws IOException {

        validateSignature(signature);
        payloadRegistry.process(event, payload, bus::publish);
    }

    private void validateSignature(String signature) {
        if (!signature.trim().isEmpty()) {
            String[] split = signature.split("=");
            if (split.length == 2) {
                String hash = split[1];
                if (isValidSHA1(hash)) {
                    return;
                }
            }
        }
        throw new RuntimeException("Nope!");
    }

    private boolean isValidSHA1(String s) {
        return s.matches("^[a-fA-F0-9]{40}$");
    }

}
