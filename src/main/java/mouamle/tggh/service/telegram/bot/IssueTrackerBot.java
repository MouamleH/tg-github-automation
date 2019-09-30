package mouamle.tggh.service.telegram.bot;

import mouamle.tggh.common.data.entity.TgUser;
import mouamle.tggh.common.data.service.DataService;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.objects.Message;

public class IssueTrackerBot extends AbilityBot {

    private final int creatorId;
    private final DataService dataService;

    public IssueTrackerBot(String token, String username, int creatorId, DataService dataService) {
        super(token, username, MapDBContext.offlineInstance("temp"));
        this.creatorId = creatorId;
        this.dataService = dataService;
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

                    TgUser tgUser = new TgUser();
                    tgUser.setGithubUsername(ghUsername);
                    tgUser.setTelegramId(ctx.user().getId());

                    dataService.saveTgUser(tgUser);

                    silent.send(String.format("Done.\nLinked to %s", ghUsername), ctx.chatId());
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
                .privacy(Privacy.CREATOR)
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

    @Override
    public org.telegram.telegrambots.meta.api.objects.User getUser(int id) {
        return super.getUser(id);
    }

    @Override
    public int creatorId() {
        return creatorId;
    }

}
