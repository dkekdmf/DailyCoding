package com.example.zero.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {

    @JsonProperty("idToken")
    private String idToken;

    @JsonProperty("email")
    private String email;

    @JsonProperty("nickname")
    private String nickname;

    public GoogleLoginRequest(){

    }
    public GoogleLoginRequest(String idToken,String email,String nickname){
        this.idToken = idToken;
        this.email = email;
        this.nickname = nickname;
    }
}