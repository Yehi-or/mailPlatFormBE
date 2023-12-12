package com.bsh.mailplatformmailservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class SendMailService {

    private final JavaMailSender javaMailSender;

    @Async
    public void sendEmail(String title, String to, String template) {
        try {
            if(title != null && to != null && template != null) {
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                //메일 제목 설정
                helper.setSubject(title);
                //수신자 설정
                helper.setTo(to);
                //html 설정
                helper.setText(template, true);
                //메일 보내기
                javaMailSender.send(message);
            }
        } catch(Exception e) {
            log.info("mail error : {}", e.toString());
        }
    }
}
