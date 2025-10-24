package com.hackathon.sos.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    // 실제 프로젝트에서는 JavaMailSender를 주입받아 사용
    // private final JavaMailSender mailSender;

    @Value("${notification.sms.enabled}")
    private Boolean smsEnabled;

    @Value("${notification.email.enabled}")
    private Boolean emailEnabled;

    @Value("${notification.email.from}")
    private String emailFrom;

    /**
     * SMS 전송
     * (실제 구현 시 Twilio, AWS SNS, 네이버 클라우드, 알리고 등의 SMS 서비스 API 사용)
     */
    public void sendSMS(String phoneNumber, String message) {
        if (!smsEnabled) {
            logger.info("SMS 전송이 비활성화되어 있습니다");
            return;
        }

        try {
            logger.info("SMS 전송 시작: to={}", phoneNumber);

            // TODO: 실제 SMS API 연동
            // 예시: Twilio, AWS SNS, 네이버 클라우드 SENS, 알리고 등

            /*
            // Twilio 예시
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            Message twilioMessage = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(FROM_PHONE_NUMBER),
                message
            ).create();

            logger.info("SMS 전송 성공: sid={}", twilioMessage.getSid());
            */

            // 개발 환경에서는 로그로 대체
            logger.info("=== SMS 전송 (개발 모드) ===");
            logger.info("수신자: {}", phoneNumber);
            logger.info("내용:\n{}", message);
            logger.info("==========================");

        } catch (Exception e) {
            logger.error("SMS 전송 실패: to={}, error={}", phoneNumber, e.getMessage(), e);
            throw new RuntimeException("SMS 전송에 실패했습니다", e);
        }
    }

    /**
     * 이메일 전송
     */
    public void sendEmail(String to, String subject, String text) {
        if (!emailEnabled) {
            logger.info("이메일 전송이 비활성화되어 있습니다");
            return;
        }

        try {
            logger.info("이메일 전송 시작: to={}, subject={}", to, subject);

            // TODO: 실제 JavaMailSender 사용
            /*
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(emailFrom);
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(text);

            mailSender.send(mailMessage);
            logger.info("이메일 전송 성공");
            */

            // 개발 환경에서는 로그로 대체
            logger.info("=== 이메일 전송 (개발 모드) ===");
            logger.info("발신자: {}", emailFrom);
            logger.info("수신자: {}", to);
            logger.info("제목: {}", subject);
            logger.info("내용:\n{}", text);
            logger.info("================================");

        } catch (Exception e) {
            logger.error("이메일 전송 실패: to={}, error={}", to, e.getMessage(), e);
            // 이메일 전송 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }

    /**
     * 푸시 알림 전송 (향후 구현)
     */
    public void sendPushNotification(String deviceToken, String title, String body) {
        logger.info("푸시 알림 전송: deviceToken={}, title={}", deviceToken, title);

        // TODO: Firebase Cloud Messaging (FCM) 연동
        /*
        Message fcmMessage = Message.builder()
            .setToken(deviceToken)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .build();

        String response = FirebaseMessaging.getInstance().send(fcmMessage);
        logger.info("푸시 알림 전송 성공: {}", response);
        */
    }
}