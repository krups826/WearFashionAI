package com.virtualtryon.Service.impl;

import com.virtualtryon.Email.EmailService;
import com.virtualtryon.Entity.Role;
import com.virtualtryon.Entity.User;
import com.virtualtryon.Entity.VerificationToken;
import com.virtualtryon.Util.OtpGenerator;
import com.virtualtryon.Dto.*;
import com.virtualtryon.Repository.RoleRepository;
import com.virtualtryon.Repository.UserRepository;
import com.virtualtryon.Repository.VerificationTokenRepository;
import com.virtualtryon.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.virtualtryon.Exception.DuplicateEmailException;
import com.virtualtryon.Exception.UserNotFoundException;
import com.virtualtryon.Exception.InvalidOtpException;
import com.virtualtryon.Exception.OtpExpiredException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final VerificationTokenRepository verificationTokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final OtpGenerator otpGenerator;
        private final EmailService emailService;


    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Email already exists.");
        }

        String otp = otpGenerator.generateOtp();
        String encodedPassword = passwordEncoder.encode(request.password());
        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found."));

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(encodedPassword);
        user.setAge(request.age());
        user.setGender(request.gender());
        user.setEnabled(false);
        user.setTheme("LIGHT");
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(role);

        user = userRepository.save(user);



        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setOtp(otp);
        verificationToken.setVerified(false);
        verificationToken.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        verificationToken.setUser(user);

        verificationTokenRepository.save(verificationToken);

        emailService.sendOtp(user.getEmail(), otp);

        return new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                "Registration successful. Please verify your email."
        );
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        if (user.getEnabled() == null || !user.getEnabled()) {
            throw new RuntimeException("Account is not verified. Please verify your email.");
        }

        return new LoginResponse(
                "USER-" + user.getId(),
                "Login successful."
        );
    }

    @Override
    public UserResponse getProfile(String email) {
        return null;
    }

    @Override
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        VerificationToken verificationToken = verificationTokenRepository.findByUserEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Verification token not found."));

        if (verificationToken.isVerified()) {
            throw new RuntimeException("Account is already verified.");
        }

        if (verificationToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("OTP has expired.");
        }

        if (!verificationToken.getOtp().equals(request.otp())) {
            throw new InvalidOtpException("Invalid OTP code.");
        }

        verificationToken.setVerified(true);
        verificationTokenRepository.save(verificationToken);

        user.setEnabled(true);
        userRepository.save(user);

        return new VerifyOtpResponse("Email verified successfully.");
    }

    @Override
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        String otp = otpGenerator.generateOtp();

        VerificationToken verificationToken = verificationTokenRepository.findByUserEmail(email)
                .orElse(new VerificationToken());

        verificationToken.setUser(user);
        verificationToken.setOtp(otp);
        verificationToken.setVerified(false);
        verificationToken.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        verificationTokenRepository.save(verificationToken);

        emailService.sendOtp(email, otp);
    }
}

