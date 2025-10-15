package com.bms.auth.contoller;

import com.bms.auth.dto.request.MailRequest;
import com.bms.auth.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private EmailService emailService;

//    @PostMapping("/send")
//    public String sendMail(@RequestBody MailRequest mailRequest) {
//        emailService.sendSimpleEmail(mailRequest);
//        return "Mail sent successfully!";
//    }
}
