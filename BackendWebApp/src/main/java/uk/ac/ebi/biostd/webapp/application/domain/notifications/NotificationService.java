package uk.ac.ebi.biostd.webapp.application.domain.notifications;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.regex.Pattern;
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

    private final EmailSender emailUtil;

    @Async
    @TransactionalEventListener
    void sentActivationEmail(UserCreatedEvent userCreatedEvent) {
        User user = userCreatedEvent.getUser();
        String activationLink = getWithKey(userCreatedEvent.getActivationLink(), user.getActivationKey());

        Map<String, Object> context = ImmutableMap.of(
                "USERNAME", user.getFullName(),
                "URL", activationLink,
                "MAILTO", FROM);

        emailUtil.sendSimpleMessage(
                "BioStudy <biostudies@ebi.ac.uk>",
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

        Map<String, Object> context = ImmutableMap.of(
                "USERNAME", user.getFullName(),
                "URL", activationLink,
                "MAILTO", FROM);

        emailUtil.sendSimpleMessage(
                "BioStudy <biostudies@ebi.ac.uk>",
                user.getEmail(),
                "Biostudy DB account activation",
                context,
                "passResetMail");
    }

    private String getWithKey(String url, String key) {
        return url.replaceAll(Pattern.quote("{KEY}"), key);
    }
}
