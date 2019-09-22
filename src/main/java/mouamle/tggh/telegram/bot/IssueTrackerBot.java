package mouamle.tggh.telegram.bot;

import mouamle.tggh.common.Settings;
import mouamle.tggh.common.Tokens;
import mouamle.tggh.github.service.GithubService;
import mouamle.tggh.github.web.model.*;
import mouamle.tggh.telegram.bot.service.IssueHookHandler;
import mouamle.tggh.util.bus.EventBus;
import org.eclipse.egit.github.core.User;
import org.springframework.stereotype.Service;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class IssueTrackerBot extends AbilityBot {

    private static final String GH_TG_MAP = "GH_TG_MAP";
    private static final String ISSUES_MESSAGES_MAP = "ISSUES_MESSAGES_MAP";

    private final Settings settings;
    private final IssueHookHandler issueHandler;
    private final GithubService githubService;

    public IssueTrackerBot(Tokens tokens, Settings settings,
                           EventBus<IssueEvent> eventBus,
                           IssueHookHandler issueHandler,
                           GithubService githubService) {

        super(tokens.getTelegramToken(), settings.getBotUsername());

        this.settings = settings;
        this.issueHandler = issueHandler;
        this.githubService = githubService;

        eventBus.subscribe(issueHandler::processEvent);
        initHandlers();
    }

    private void initHandlers() {
        issueHandler.registerHandler("opened", this::handleOpened);
        issueHandler.registerHandler("closed", this::handleClosed);
        issueHandler.registerHandler("reopened", this::handleReopened);
    }

    public Ability start() {
        return Ability.builder()
                .name("start")
                .info("Starts the bot")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.USER)
                .action(ctx -> silent.sendMd("Hello!\nSend `/link <Github Username>`", ctx.chatId()))
                .build();
    }

    public Ability link() {
        return Ability.builder()
                .name("link")
                .info("Links your telegram username to your github one")
                .input(1)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.USER)
                .action(ctx -> {
                    String ghUsername = ctx.firstArg();
                    try {
                        User user = githubService.getUser(ghUsername);

                        Map<String, Integer> usersMap = getGhUsers();
                        usersMap.put(user.getLogin(), ctx.user().getId());

                        db.commit();
                        silent.send(String.format("Done.\nLinked to %s", user.getLogin()), ctx.chatId());
                    } catch (IOException e) {
                        silent.send("Nope!", ctx.chatId());
                    }
                })
                .build();
    }

    public Ability me() {
        return Ability.builder()
                .name("me")
                .info("Displays user info")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {

                })
                .build();
    }

    public Ability getId() {
        return Ability.builder()
                .name("id")
                .info("Returns ids")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .action(ctx -> {
                    StringBuilder str = new StringBuilder();
                    Message message = ctx.update().getMessage();
                    str.append("ChatID: `").append(message.getChat().getId()).append('`').append('\n');
                    str.append("UserID: `").append(message.getFrom().getId()).append('`');
                    silent.sendMd(str.toString(), ctx.chatId());
                })
                .build();
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
            Message execute = execute(new SendMessage(settings.getGroupId(), message.toString()).enableMarkdown(true));
            Map<Integer, Integer> messages = getMessages();
            messages.put(issue.getId(), execute.getMessageId());
            db.commit();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private void handleClosed(IssueEvent event) {
        Sender sender = event.getSender();
        Issue issue = event.getIssue();

        Map<Integer, Integer> messages = getMessages();
        if (!messages.containsKey(issue.getId())) {
            return;
        }

        StringBuilder message = new StringBuilder();
        String issueString = String.format("Closed [this issue](%s).", issue.getHtmlUrl());
        message.append(getProperName(sender.getLogin())).append(" ").append(issueString);

        silent.execute(new SendMessage(settings.getGroupId(), message.toString()).enableMarkdown(true).setReplyToMessageId(messages.get(issue.getId())));
    }

    private void handleReopened(IssueEvent event) {
        Sender sender = event.getSender();
        Issue issue = event.getIssue();

        Map<Integer, Integer> messages = getMessages();
        if (!messages.containsKey(issue.getId())) {
            return;
        }

        StringBuilder message = new StringBuilder();
        String issueString = String.format("Reopened [this issue](%s).", issue.getHtmlUrl());
        message.append(getProperName(sender.getLogin())).append(" ").append(issueString);

        silent.execute(new SendMessage(settings.getGroupId(), message.toString()).enableMarkdown(true).setReplyToMessageId(messages.get(issue.getId())));
    }


    private String getProperName(String ghUsername) {
        Map<String, Integer> ghUsers = getGhUsers();
        if (!ghUsers.containsKey(ghUsername)) {
            return ghUsername;
        }

        int tgUsers = ghUsers.get(ghUsername);
        String username = getUser(tgUsers).getUserName();
        if (username != null) {
            return "@" + "[" + username + "]";
        }
        return ghUsername;
    }

    // Github, Telegram ids
    private Map<String, Integer> getGhUsers() {
        return db.getMap(GH_TG_MAP);
    }

    // issueID, messageID
    private Map<Integer, Integer> getMessages() {
        return db.getMap(ISSUES_MESSAGES_MAP);
    }


    @Override
    public int creatorId() {
        return settings.getCreatorId();
    }

}
