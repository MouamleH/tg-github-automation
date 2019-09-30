package mouamle.tggh.common.providers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Github group data values
 */
@Component
public class Github implements TokenProvider {

    @Value("${app.github-token}")
    private String token;

    public String getToken() {
        return token;
    }

}
