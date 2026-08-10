package com.smconsole.common.exception;

//그 결정한 내용을, 어떤 형태로 포장해서 보낼지 정의하는 곳
public class ErrorResponse {

    private String message;

    public ErrorResponse(String message){
        this.message = message;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

