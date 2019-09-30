package mouamle.tggh.common.data.entity;

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "TgUser.findByGithubUsername",
                query = "SELECT u FROM TgUser u WHERE u.githubUsername = :githubUsername")
})
public class TgUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int telegramId;

    private String githubUsername;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(int telegramId) {
        this.telegramId = telegramId;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

}
