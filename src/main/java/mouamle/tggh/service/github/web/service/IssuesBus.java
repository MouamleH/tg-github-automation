package mouamle.tggh.service.github.web.service;

import mouamle.tggh.service.github.web.model.IssueEvent;
import mouamle.tggh.common.bus.AbstractEventBus;
import org.springframework.stereotype.Service;

/**
 * {@link mouamle.tggh.common.bus.EventBus} Implementation for the {@link IssueEvent}
 */
@Service
public class IssuesBus extends AbstractEventBus<IssueEvent> { }
