package uk.ac.ebi.biostd.webapp.application.domain.services;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.ac.ebi.biostd.webapp.application.common.email.EmailSender;
import uk.ac.ebi.biostd.webapp.application.domain.events.PassResetEvent;
import uk.ac.ebi.biostd.webapp.application.domain.events.UserCreatedEvent;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;

@Service
@AllArgsConstructor
public class NotificationService {

    private static final String FROM = "biostudies@ebi.ac.uk";
    private static final String EMAIL_FROM = "BioStudy <biostudies@ebi.ac.uk>";

    private final EmailSender emailUtil;

    @Async
    @TransactionalEventListener
    void sentActivationEmail(UserCreatedEvent userCreatedEvent) {
        User user = userCreatedEvent.getUser();
        String activationLink = getWithKey(userCreatedEvent.getActivationLink(), user.getActivationKey());

        Map<String, Object> context = getBaseContextMap(user, activationLink);

        emailUtil.sendSimpleMessage(
                EMAIL_FROM,
                user.getEmail(),
                "Biostudy DB account activation",
                context,
                "activationMail");
    }

    @Async
    @TransactionalEventListener
    void sentResetEmail(PassResetEvent passResetRequest) {
        User user = passResetRequest.getUser();
        String activationLink = getWithKey(passResetRequest.getActivationLink(), user.getActivationKey());
        Map<String, Object> context = getBaseContextMap(user, activationLink);

        emailUtil.sendSimpleMessage(
                EMAIL_FROM,
                user.getEmail(),
                "Biostudy DB account activation",
                context,
                "passResetMail");
    }

    private Map<String, Object> getBaseContextMap(User user, String activationLink) {
        return ImmutableMap.of(
                "USERNAME", user.getFullName(),
                "URL", activationLink,
                "MAILTO", FROM);
    }

    private String getWithKey(String url, String key) {
        return String.format("%s/%s", url, key);
    }
}
