package mouamle.tggh.common.providers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Discord group data values
 */
@Component
public class Discord implements TokenProvider {

    @Value("${app.discord-token}")
    private String token;

    @Value("${app.discord-channel-id}")
    private String channelId;

    public String getToken() {
        return token;
    }

    public String getChannelId() {
        return channelId;
    }

}
