package mouamle.tggh.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Tokens {

    private final String telegramToken;
    private final String githubToken;

    public Tokens(@Value("${app.telegram-token}") String telegramToken,
                  @Value("${app.github-token}") String githubToken) {
        this.telegramToken = telegramToken;
        this.githubToken = githubToken;
    }


    public String getTelegramToken() {
        return telegramToken;
    }

    public String getGithubToken() {
        return githubToken;
    }

}
