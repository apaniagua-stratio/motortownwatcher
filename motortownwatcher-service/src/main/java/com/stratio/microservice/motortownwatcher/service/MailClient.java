package com.stratio.microservice.motortownwatcher.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MailClient {

    private JavaMailSender mailSender;

    @Autowired
    public MailClient(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired
    private MailContentBuilder mailContentBuilder;

    public void prepareAndSend(String sender, String recipient, String subject, String message) {

        final String subjectDate= LocalDateTime.now().plusHours(2) + " " +  subject;

            MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(sender);
            messageHelper.setTo(recipient);
            messageHelper.setSubject(subjectDate);

            String content = mailContentBuilder.build(message);
            //messageHelper.setText(content, true);
            messageHelper.setText(message);

        };
        try {
            mailSender.send(messagePreparator);
        } catch (MailException e) {
            // runtime exception; compiler will not force you to handle it
        }

    }

}
