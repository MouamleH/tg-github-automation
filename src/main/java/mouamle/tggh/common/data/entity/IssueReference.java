package mouamle.tggh.common.data.entity;

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "IssueReference.findByIssueIdAndRepoId",
                query = "SELECT ir FROM IssueReference ir WHERE ir.repoId = :repoId AND ir.issueId = :issueId")
})
public class IssueReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int repoId;
    private int issueId;

    private int tgMessageId;
    private String discordMessageId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRepoId() {
        return repoId;
    }

    public void setRepoId(int repoId) {
        this.repoId = repoId;
    }

    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public int getTgMessageId() {
        return tgMessageId;
    }

    public void setTgMessageId(int tgMessageId) {
        this.tgMessageId = tgMessageId;
    }

    public String getDiscordMessageId() {
        return discordMessageId;
    }

    public void setDiscordMessageId(String discordMessageId) {
        this.discordMessageId = discordMessageId;
    }

}
