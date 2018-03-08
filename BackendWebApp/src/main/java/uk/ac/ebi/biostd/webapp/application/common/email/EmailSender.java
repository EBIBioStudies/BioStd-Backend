package uk.ac.ebi.biostd.webapp.application.common.email;

import java.util.Map;
import javax.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@AllArgsConstructor
public class EmailSender {

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    @SneakyThrows
    public void sendSimpleMessage(String from, String to, String subject, Map<String, Object> context,
            String template) {
        MimeMessage mail = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mail, true);
        helper.setTo(to);
        helper.setFrom(from);
        helper.setSubject(subject);
        helper.setText(getText(context, template), true);
        emailSender.send(mail);
    }

    private String getText(Map<String, Object> data, String template) {
        Context ctx = new Context();
        data.forEach(ctx::setVariable);
        return templateEngine.process(template, ctx);
    }
}
