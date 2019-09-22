package mouamle.tggh.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Settings {

    private final String botUsername;
    private final int creatorId;
    private final long groupId;

    public Settings(@Value("${app.telegram-username}") String botUsername,
                    @Value("${app.telegram-creatorId}") int creatorId,
                    @Value("${app.telegram-groupId}") long groupId) {
        this.botUsername = botUsername;
        this.creatorId = creatorId;
        this.groupId = groupId;
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
