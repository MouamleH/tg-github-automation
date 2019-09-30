package mouamle.tggh.common.providers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Telegram group data values
 */
@Component
public class Telegram implements TokenProvider {

    @Value("${app.telegram-token}")
    private String token;

    @Value("${app.telegram-username}")
    private String botUsername;

    @Value("${app.telegram-creatorId}")
    private int creatorId;

    @Value("${app.telegram-groupId}")
    private long groupId;

    public String getToken() {
        return token;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public long getGroupId() {
        return groupId;
    }

}
