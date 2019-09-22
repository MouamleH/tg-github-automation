package mouamle.tggh.github.web.service;

import mouamle.tggh.github.web.model.IssueEvent;
import mouamle.tggh.util.bus.AbstractEventBus;
import org.springframework.stereotype.Service;

@Service
public class IssuesBus extends AbstractEventBus<IssueEvent> { }
