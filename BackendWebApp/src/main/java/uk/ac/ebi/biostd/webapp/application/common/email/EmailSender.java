package uk.ac.ebi.biostd.webapp.application.common.email;

import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@AllArgsConstructor
public class EmailSender {

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    public void sendSimpleMessage(String from, String to, String subject, Map<String, Object> context,
            String template) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(getText(context, template));
        emailSender.send(message);
    }

    private String getText(Map<String, Object> data, String template) {
        Context ctx = new Context();
        data.forEach(ctx::setVariable);
        return templateEngine.process(template, ctx);
    }
}
