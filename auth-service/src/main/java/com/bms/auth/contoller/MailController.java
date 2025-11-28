package com.bms.auth.contoller;

import com.bms.auth.dto.request.MailRequest;
import com.bms.auth.service.EmailService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
public class MailController {

    private final EmailService emailService;

    public MailController(EmailService emailService) {
        this.emailService = emailService;
    }

    // @PostMapping("/send")
    // public String sendMail(@RequestBody MailRequest mailRequest) {
    // emailService.sendSimpleEmail(mailRequest);
    // return "Mail sent successfully!";
    // }
}
