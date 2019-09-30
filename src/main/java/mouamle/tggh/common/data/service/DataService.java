package mouamle.tggh.common.data.service;

import mouamle.tggh.common.data.entity.IssueReference;
import mouamle.tggh.common.data.entity.TgUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Service
public class DataService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Saves/Updates the reference to the (new issue) message on telegram/discord
     * @param reference the reference
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public synchronized void saveIssueReference(IssueReference reference) {
        Optional<IssueReference> oReference = getIssueReference(reference.getRepoId(), reference.getIssueId());
        if (oReference.isPresent()) {
            IssueReference issueReference = oReference.get();

            if (reference.getDiscordMessageId() != null) {
                issueReference.setDiscordMessageId(reference.getDiscordMessageId());
            }

            if (reference.getTgMessageId() != 0) {
                issueReference.setTgMessageId(reference.getTgMessageId());
            }

            em.persist(issueReference);
        } else {
            em.persist(reference);
        }
    }

    /**
     * Saves/Updates the user github username/telegram id
     * @param user the user
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveTgUser(TgUser user) {
        Optional<TgUser> oTgUser = getTgUser(user.getGithubUsername());
        if (oTgUser.isPresent()) {
            TgUser tgUser = oTgUser.get();
            tgUser.setTelegramId(user.getTelegramId());
            em.persist(tgUser);
        } else {
            em.persist(user);
        }
    }

    /**
     * Returns the (new issue) message reference on telegram/discord
     * @param repoId the repository id
     * @param issueId the issue id
     * @return the reference for the message
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public Optional<IssueReference> getIssueReference(int repoId, int issueId) {
        TypedQuery<IssueReference> query = em.createNamedQuery("IssueReference.findByIssueIdAndRepoId", IssueReference.class);
        query.setParameter("repoId", repoId);
        query.setParameter("issueId", issueId);
        List<IssueReference> result = query.getResultList();
        if (!result.isEmpty()) {
            return Optional.of(result.get(0));
        }

        return Optional.empty();
    }

    /**
     * Returns the user by it's github name
     * @param githubUsername the user github username
     * @return the user reference
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public Optional<TgUser> getTgUser(String githubUsername) {
        TypedQuery<TgUser> query = em.createNamedQuery("TgUser.findByGithubUsername", TgUser.class);
        query.setParameter("githubUsername", githubUsername);
        List<TgUser> result = query.getResultList();
        if (!result.isEmpty()) {
            return Optional.of(result.get(0));
        }

        return Optional.empty();
    }

}
