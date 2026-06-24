package com.battery.entity;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String nickname;

    public LoginResponse(String token, String username, String nickname) {
        this.token = token;
        this.username = username;
        this.nickname = nickname;
    }
}
