package com.devopsbuddy.model;

public class AskResponse {
    private String response;

    public AskResponse(String response){
        this.response = response;
    }

    public String getResponse(){
        return response;
    }

    public void setResponse(String response){
        this.response = response;
    }
}
