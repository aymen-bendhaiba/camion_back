package com.example.demo.sec.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorResponse {
    private String qrCodeUrl;
    private String secretKey;
    private String message;
    private boolean success;
} 