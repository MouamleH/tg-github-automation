package mouamle.tggh.telegram;

import mouamle.tggh.common.ServiceInterface;
import mouamle.tggh.telegram.bot.IssueTrackerBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Service
public class TelegramInterface implements ServiceInterface {

    private final Logger mLogger = LoggerFactory.getLogger(TelegramInterface.class);

    private final IssueTrackerBot bot;

    @Autowired
    public TelegramInterface(IssueTrackerBot bot) {
        this.bot = bot;
    }

    @Override
    public void init() {
        new Thread(() -> {
            ApiContextInitializer.init();
            TelegramBotsApi api = new TelegramBotsApi();

            try {
                api.registerBot(bot);
                mLogger.info("Telegram bot started");
            } catch (TelegramApiRequestException e) {
                mLogger.error(e.getMessage(), e);
            }

        }, "Telegram Thread").start();
    }

}
