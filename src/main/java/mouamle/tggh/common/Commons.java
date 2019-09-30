package mouamle.tggh.common;

import mouamle.tggh.common.handler.IssueEventHandler;
import mouamle.tggh.service.github.web.model.IssueEvent;
import mouamle.tggh.common.bus.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Subscribes to the IssueEvent bus then broadcasts the data to the IssueEventHandler
 * to be processed by other classes
 */
@Service
public class Commons {

    @Autowired
    public Commons(EventBus<IssueEvent> bus, IssueEventHandler issueHandler) {
        bus.subscribe(issueHandler::processEvent);
    }

}
