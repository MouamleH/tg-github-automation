package mouamle.tggh.service.telegram;

import mouamle.tggh.common.ServiceInterface;
import mouamle.tggh.common.data.entity.IssueReference;
import mouamle.tggh.common.data.entity.TgUser;
import mouamle.tggh.common.data.service.DataService;
import mouamle.tggh.common.handler.IssueEventHandler;
import mouamle.tggh.common.providers.Telegram;
import mouamle.tggh.service.github.web.model.*;
import mouamle.tggh.service.telegram.bot.IssueTrackerBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.List;
import java.util.Optional;

/**
 * Telegram interface
 */
@Service
public class TelegramInterface implements ServiceInterface {

    private final Logger mLogger = LoggerFactory.getLogger(TelegramInterface.class);

    private final Telegram telegram;
    private final DataService dataService;
    private final IssueEventHandler issuesHandler;

    private IssueTrackerBot bot;

    @Autowired
    public TelegramInterface(Telegram telegram,
                             DataService dataService,
                             IssueEventHandler issuesHandler) {
        this.telegram = telegram;
        this.dataService = dataService;
        this.issuesHandler = issuesHandler;
    }

    @Override
    public void init() {
        mLogger.info("Setting up Telegram bot api");
        initTelegramApi();

        mLogger.info("Setting up event handlers");
        initHandlers();
    }

    private void initTelegramApi() {
        ApiContextInitializer.init();
        TelegramBotsApi api = new TelegramBotsApi();
        try {
            bot = new IssueTrackerBot(telegram.getToken(), telegram.getBotUsername(), telegram.getCreatorId(), dataService);
            api.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            mLogger.error(e.getMessage(), e);
        }
    }

    private void initHandlers() {
        issuesHandler.registerHandler(IssueEvent.OPENED, this::handleOpened);
        issuesHandler.registerHandler(IssueEvent.CLOSED, this::handleClosed);
        issuesHandler.registerHandler(IssueEvent.REOPENED, this::handleReopened);
    }

    private void handleOpened(IssueEvent event) {
        Issue issue = event.getIssue();

        StringBuilder message = new StringBuilder();
        message.append("*New Issue:* ").append(issue.getTitle()).append("\n");
        if (issue.getBody().length() > 140) {
            message.append(issue.getBody(), 0, 140)
                    .append("... ")
                    .append("[read more]").append("(").append(issue.getHtmlUrl()).append(")")
                    .append("\n\n");
        } else {
            message.append(issue.getBody()).append("\n\n");
        }

        Sender sender = event.getSender();
        message.append("*Created by:* ");
        message.append(getProperName(sender.getLogin())).append("\n\n");

        List<Assignee> assignees = issue.getAssignees();
        if (!assignees.isEmpty()) {
            message.append("*Assignees:*").append("\n");

            for (Assignee assignee : assignees) {
                String ghUsername = assignee.getLogin();
                message.append(" -").append(getProperName(ghUsername)).append("\n");
            }
            message.append("\n");
        }

        List<Label> labels = issue.getLabels();
        if (!labels.isEmpty()) {
            message.append("*Labels:*").append("\n");
            for (Label label : labels) {
                message.append(String.format(" -[%s](%s)", label.getName(), label.getUrl())).append("\n");
            }
        }

        try {
            SendMessage send = new SendMessage();
            send.setChatId(telegram.getGroupId());
            send.setText(message.toString());
            send.enableMarkdown(true);

            Message sentMessage = bot.execute(send);

            IssueReference reference = new IssueReference();
            reference.setRepoId(event.getRepository().getId());
            reference.setIssueId(issue.getId());
            reference.setTgMessageId(sentMessage.getMessageId());
            dataService.saveIssueReference(reference);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleClosed(IssueEvent event) {
        Sender sender = event.getSender();
        Issue issue = event.getIssue();

        Optional<IssueReference> oReference = dataService.getIssueReference(event.getRepository().getId(), issue.getId());
        if (!oReference.isPresent()) {
            return;
        }

        IssueReference reference = oReference.get();

        StringBuilder message = new StringBuilder();
        String issueString = String.format("Closed [this issue](%s).", issue.getHtmlUrl());
        message.append(getProperName(sender.getLogin())).append(" ").append(issueString);

        SendMessage send = new SendMessage();
        send.setChatId(telegram.getGroupId());
        send.setText(message.toString());
        send.enableMarkdown(true);
        send.setReplyToMessageId(reference.getTgMessageId());

        try {
            bot.execute(send);
        } catch (TelegramApiException e) {
            mLogger.error(e.getMessage(), e);
        }
    }

    private void handleReopened(IssueEvent event) {
        Sender sender = event.getSender();
        Issue issue = event.getIssue();

        Optional<IssueReference> oReference = dataService.getIssueReference(event.getRepository().getId(), issue.getId());
        if (!oReference.isPresent()) {
            return;
        }

        IssueReference reference = oReference.get();

        StringBuilder message = new StringBuilder();
        String issueString = String.format("Reopened [this issue](%s).", issue.getHtmlUrl());
        message.append(getProperName(sender.getLogin())).append(" ").append(issueString);

        SendMessage send = new SendMessage();
        send.setChatId(telegram.getGroupId());
        send.setText(message.toString());
        send.enableMarkdown(true);
        send.setReplyToMessageId(reference.getTgMessageId());

        try {
            bot.execute(send);
        } catch (TelegramApiException e) {
            mLogger.error(e.getMessage(), e);
        }
    }

    private String getProperName(String ghUsername) {
        Optional<TgUser> oTgUser = dataService.getTgUser(ghUsername);
        if (!oTgUser.isPresent()) {
            return ghUsername;
        }

        TgUser tgUser = oTgUser.get();
        String username = bot.getUser(tgUser.getTelegramId()).getUserName();
        if (username != null) {
            return "@" + "[" + username + "]";
        }
        return ghUsername;
    }

}
