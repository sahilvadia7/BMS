package com.bms.notification.consumer;

import com.bms.notification.dto.UserEvent;
import com.bms.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
//import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventConsumer {

//    private final EmailService emailService;
//
//
//    @KafkaListener(topics = "user-registered", groupId = "notification-group")
//    public void handleUserRegistered(UserEvent event) {
//        String subject = "Welcome to Our Bank!";
//        String body = "Hello " + event.getUsername() + ", welcome to our bank!";
//        emailService.sendEmail(event.getEmail(), subject, body);
//    }
//
//    @KafkaListener(topics = "user-login", groupId = "notification-group")
//    public void handleUserLogin(UserEvent event) {
//        String subject = "Login Alert";
//        String body = "Hello " + event.getUsername() + ", you just logged in at " + event.getLoginDate();
//        emailService.sendEmail(event.getEmail(), subject, body);
//    }

}
