package com.virtualtryon.Controller;

import com.virtualtryon.Dto.LoginRequest;
import com.virtualtryon.Dto.LoginResponse;
import com.virtualtryon.Dto.RegisterRequest;
import com.virtualtryon.Dto.RegisterResponse;
import com.virtualtryon.Dto.VerifyOtpRequest;
import com.virtualtryon.Dto.VerifyOtpResponse;
import com.virtualtryon.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request){
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Void> resendOtp(@RequestParam String email){
        authService.resendOtp(email);
        return ResponseEntity.ok().build();
    }
}
