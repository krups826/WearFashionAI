package com.virtualtryon.Exception;

public class OtpExpiredException extends RuntimeException{

    public OtpExpiredException(String message){
        super(message);
    }
}
