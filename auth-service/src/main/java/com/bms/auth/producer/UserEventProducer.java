package com.bms.auth.producer;

import com.bms.auth.dto.UserEvent;

//import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventProducer {

    // private final KafkaTemplate<String, Object> kafkaTemplate;
    //
    // public void sendUserRegisteredEvent(UserEvent event) {
    // kafkaTemplate.send("user-registered", event);
    // System.out.println("User registered event sent: " + event);
    // }
    //
    // public void sendUserLoginEvent(UserEvent event) {
    // kafkaTemplate.send("user-login", event);
    // System.out.println("User login event sent: " + event);
    // }

}
