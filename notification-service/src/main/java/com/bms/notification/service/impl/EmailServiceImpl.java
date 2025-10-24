package com.bms.notification.service.impl;

import com.bms.notification.dto.request.EmailRequestDTO;
import com.bms.notification.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendRegistrationEmail(EmailRequestDTO  requestDTO) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(requestDTO.getToEmail());
            helper.setSubject("Welcome to BMS — Your Account is Ready!");

            String content = """
                    <html>
                    <body style="font-family: Arial, sans-serif; color: #333;">
                        <h2>Dear %s,</h2>
                        <p>Welcome to <b>BMS Online Banking</b></p>
                        <p>Your registration is successful. You can now log in using your credentials.</p>
                        <p><b>CIF ID:</b> %s</p>
                        <p>Use the password you set during registration to log in.</p>
                        <br/>
                        <p>Regards,<br/>BMS Team</p>
                    </body>
                    </html>
                    """.formatted(requestDTO.getCustomerName(), requestDTO.getCifId());

            helper.setText(content, true);
            mailSender.send(message);

            System.out.println("Email sent successfully to: " + requestDTO.getToEmail());
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}
