package com.bms.notification.service.impl;

import com.bms.notification.dto.OtpEmailDTO;
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
        <body style="font-family: Arial, sans-serif; color: #333; background-color: #f9f9f9; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background: #fff; padding: 30px; border-radius: 10px; box-shadow: 0 2px 6px rgba(0,0,0,0.1);">
                <h2 style="color: #4a148c;">Dear %s,</h2>
                <p>Welcome to <b>BMS Online Banking</b>!</p>
                <p>Your <b>profile has been successfully created</b> in our system.</p>
                <p><b>CIF ID:</b> %s</p>
                <p>Your login credentials have been sent to your registered email address. 
                   Please check your inbox and use those credentials to log in and complete your KYC verification.</p>
                <br/>
                <p style="margin-top: 30px;">Regards,<br/><b>BMS Team</b></p>
            </div>
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
    @Override
    public void sendOtpEmail(OtpEmailDTO request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(request.getToEmail());
            helper.setSubject("BMS - Password Reset OTP");

            String content = """
            <html>
            <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6;">
                <h2>Dear %s,</h2>
                <p>We received a request to reset your password for your <b>BMS Online Banking</b> account.</p>
                <p>Your One-Time Password (OTP) is:</p>
                <div style="background: #f3e5f5; padding: 15px; border-radius: 8px; text-align: center;">
                    <h1 style="color: #4a148c; letter-spacing: 4px;">%s</h1>
                </div>
                <p>This OTP is valid for <b>5 minutes</b>. Please do not share it with anyone.</p>
                <br/>
                <p>If you did not request a password reset, you can safely ignore this email.</p>
                <br/>
                <p>Regards,<br/><b>BMS Team</b></p>
            </body>
            </html>
            """.formatted(request.getCustomerName(), request.getOtp());

            helper.setText(content, true);
            mailSender.send(message);

            System.out.println("OTP email sent successfully to: " + request.getToEmail());
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
        }
    }


}
