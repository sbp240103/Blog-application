package com.project.app.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.project.app.services.MailService;

// development mail sender, writes the mail to the log instead of sending it.
// swap with a real smtp implementation for production
@Service
public class LoggingMailService implements MailService {

    private static final Logger log = LoggerFactory.getLogger(LoggingMailService.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("mail to={} subject=\"{}\"\n{}", to, subject, body);
    }
}
