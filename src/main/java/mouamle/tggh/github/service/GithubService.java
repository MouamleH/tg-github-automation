package mouamle.tggh.github.service;

import mouamle.tggh.common.Tokens;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GithubService {

    private GitHubClient client;

    public GithubService(Tokens tokens) {
        client = new GitHubClient();
        client.setOAuth2Token(tokens.getGithubToken());
    }

    public GitHubClient getClient() {
        return client;
    }

    public User getUser(String name) throws IOException {
        UserService service = new UserService(client);
        return service.getUser(name);
    }

}
