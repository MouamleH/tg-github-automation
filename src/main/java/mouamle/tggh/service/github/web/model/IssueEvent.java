package mouamle.tggh.service.github.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "action",
        "issue",
        "changes",
        "repository",
        "sender"
})
public class IssueEvent implements Serializable {

    public static final String OPENED = "opened";
    public static final String CLOSED = "closed";
    public static final String REOPENED = "reopened";

    private final static long serialVersionUID = 6031004231680580358L;
    @JsonProperty("action")
    public String action;
    @JsonProperty("issue")
    public Issue issue;
    @JsonProperty("changes")
    public Changes changes;
    @JsonProperty("repository")
    public Repository repository;
    @JsonProperty("sender")
    public Sender sender;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public Changes getChanges() {
        return changes;
    }

    public void setChanges(Changes changes) {
        this.changes = changes;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }
}
