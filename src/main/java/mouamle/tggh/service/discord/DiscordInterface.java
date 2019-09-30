package mouamle.tggh.service.discord;

import mouamle.tggh.common.ServiceInterface;
import mouamle.tggh.common.data.entity.IssueReference;
import mouamle.tggh.common.data.service.DataService;
import mouamle.tggh.common.handler.IssueEventHandler;
import mouamle.tggh.common.providers.Discord;
import mouamle.tggh.service.github.web.model.*;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Discord interface
 */
@Service
public class DiscordInterface implements ServiceInterface {

    private final Logger mLogger = LoggerFactory.getLogger(DiscordInterface.class);

    private final Discord discord;
    private final DataService dataService;
    private final IssueEventHandler issuesHandler;

    private static final String EMOJI_LOCKED = "\uD83D\uDD12";
    private static final String EMOJI_UNLOCKED = "\uD83D\uDD13";

    private static final int COLOR_REOPENED = 0x27ae60; // Green
    private static final int COLOR_CLOSED = 0xc0392b; // Red
    private static final int COLOR_INFO = 0xf39c12; // Orange

    private TextChannel textChannel;

    @Autowired
    public DiscordInterface(Discord discord,
                            DataService dataService,
                            IssueEventHandler issuesHandler) {
        this.discord = discord;
        this.dataService = dataService;
        this.issuesHandler = issuesHandler;
    }

    @Override
    public void init() {
        mLogger.info("Setting up JDA");
        initJDA();

        mLogger.info("Setting up event handlers");
        initHandlers();
    }

    private void initJDA() {
        try {
            JDA jda = new JDABuilder(discord.getToken()).build();
            jda.addEventListener(new ListenerAdapter() {
                public void onReady(ReadyEvent event) {
                    textChannel = jda.getTextChannelById(discord.getChannelId());
                }
            });
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    private void initHandlers() {
        issuesHandler.registerHandler(IssueEvent.OPENED, this::handleOpened);
        issuesHandler.registerHandler(IssueEvent.CLOSED, this::handleClosed);
        issuesHandler.registerHandler(IssueEvent.REOPENED, this::handleReopened);
    }

    private void handleOpened(IssueEvent event) {
        Issue issue = event.getIssue();
        Sender sender = event.getSender();

        List<Assignee> assigneesList = issue.getAssignees();
        List<Label> labelsList = issue.getLabels();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("**New Issue**");
        builder.setAuthor(sender.getLogin(), sender.getHtmlUrl(), sender.getAvatarUrl());

        StringBuilder issueBody = new StringBuilder();
        issueBody.append("**").append(issue.getTitle()).append("**");
        issueBody.append("\n\n");

        if (issue.getBody().length() > 140) {
            issueBody.append(issue.getBody(), 0, 140)
                    .append("... ")
                    .append("[read more]").append("(").append(issue.getHtmlUrl()).append(")")
                    .append("\n\n");
        } else {
            issueBody.append(issue.getBody()).append("\n\n");
        }
        builder.setDescription(issueBody);

        if (!assigneesList.isEmpty()) {
            String assignees = assigneesList.stream().map(user -> "- " + user.getLogin()).collect(Collectors.joining("\n"));
            builder.addField("**Assignees**", assignees, false);
        }

        if (!labelsList.isEmpty()) {
            String labels = labelsList.stream().map(label -> "- " + label.getName()).collect(Collectors.joining("\n"));
            builder.addField("**Labels**", labels, false);
        }

        textChannel.sendMessage(builder.build()).queue(sentMessage -> {
            IssueReference reference = new IssueReference();
            reference.setRepoId(event.getRepository().getId());
            reference.setIssueId(issue.getId());

            reference.setDiscordMessageId(sentMessage.getId());
            dataService.saveIssueReference(reference);
        });
    }

    private void handleClosed(IssueEvent event) {
        Sender sender = event.getSender();
        Issue issue = event.getIssue();

        Optional<IssueReference> oReference = dataService.getIssueReference(event.getRepository().getId(), issue.getId());
        if (!oReference.isPresent()) {
            return;
        }

        IssueReference reference = oReference.get();
        Message issueMessage = textChannel.getMessageById(reference.getDiscordMessageId()).complete();
        MessageEmbed issueMessageEmbed = issueMessage.getEmbeds().get(0);

        EmbedBuilder issueMessageBuilder = new EmbedBuilder(issueMessageEmbed);
        issueMessageBuilder.setTitle(String.format("**New Issue** (%s)", EMOJI_LOCKED + " closed"));
        issueMessageBuilder.setColor(COLOR_CLOSED);
        issueMessage.editMessage(issueMessageBuilder.build()).queue();


        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(sender.getLogin(), sender.getHtmlUrl(), sender.getAvatarUrl());
        builder.setTitle("**Closed Issue**");
        builder.setColor(COLOR_INFO);

        StringBuilder issueBody = new StringBuilder();
        issueBody.append("**").append(issue.getTitle()).append("**");
        issueBody.append("\n");
        issueBody.append("[View Issue]").append("(").append(issue.getHtmlUrl()).append(")");

        builder.setDescription(issueBody);

        textChannel.sendMessage(builder.build()).queue();
    }

    private void handleReopened(IssueEvent event) {
        Sender sender = event.getSender();
        Issue issue = event.getIssue();

        Optional<IssueReference> oReference = dataService.getIssueReference(event.getRepository().getId(), issue.getId());
        if (!oReference.isPresent()) {
            return;
        }

        IssueReference reference = oReference.get();
        Message issueMessage = textChannel.getMessageById(reference.getDiscordMessageId()).complete();
        MessageEmbed issueMessageEmbed = issueMessage.getEmbeds().get(0);

        EmbedBuilder issueMessageBuilder = new EmbedBuilder(issueMessageEmbed);
        issueMessageBuilder.setTitle(String.format("**New Issue** (%s)", EMOJI_UNLOCKED + " reopened"));
        issueMessageBuilder.setColor(COLOR_REOPENED);
        issueMessage.editMessage(issueMessageBuilder.build()).queue();


        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(sender.getLogin(), sender.getHtmlUrl(), sender.getAvatarUrl());
        builder.setTitle("**Reopened Issue**");
        builder.setColor(COLOR_INFO);

        StringBuilder issueBody = new StringBuilder();
        issueBody.append("**").append(issue.getTitle()).append("**");
        issueBody.append("\n");
        issueBody.append("[View Issue]").append("(").append(issue.getHtmlUrl()).append(")");

        builder.setDescription(issueBody);

        textChannel.sendMessage(builder.build()).queue();
    }

}
