package com.example.doantn.service;

import com.example.doantn.dto.auth.AuthResponse;
import com.example.doantn.dto.auth.LoginRequest;
import com.example.doantn.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
} 