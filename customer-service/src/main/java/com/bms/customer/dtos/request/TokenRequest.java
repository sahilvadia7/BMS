package com.bms.customer.dtos.request;


public class TokenRequest {
    private String refreshToken;

    public TokenRequest() {}

    public TokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

