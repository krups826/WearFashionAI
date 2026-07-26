package com.virtualtryon.Service;

import com.virtualtryon.Dto.*;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse getProfile(String email);

    VerifyOtpResponse verifyOtp(VerifyOtpRequest request);

    void resendOtp(String email);

}
