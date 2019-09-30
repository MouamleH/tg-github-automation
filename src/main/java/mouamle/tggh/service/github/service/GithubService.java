package mouamle.tggh.service.github.service;

import mouamle.tggh.common.providers.Github;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Github api interface
 */
@Component
public class GithubService {

    private GitHubClient client;

    public GithubService(Github github) {
        client = new GitHubClient();
        client.setOAuth2Token(github.getToken());
    }

    public GitHubClient getClient() {
        return client;
    }

    public User getUser(String name) throws IOException {
        UserService service = new UserService(client);
        return service.getUser(name);
    }

}
