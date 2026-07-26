package com.virtualtryon.Util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@Component
public class OtpGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateOtp(){

        int otp = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }
}
