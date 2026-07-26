package com.virtualtryon.Email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{

    private final JavaMailSender mailSender;

    @Override
    public void sendOtp(String to,String otp){
        System.out.println("==================================");
        System.out.println("Thank You For Joining on VirtualTryOn");
        System.out.println("Email : " + to);
        System.out.println("OTP   : " + otp);
        System.out.println("==================================");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("WearFashion - Email Verification OTP");
            message.setText("Welcome to WearFashion!\n\nYour registration verification code (OTP) is: " + otp + "\n\nThis OTP is valid for 5 minutes.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
