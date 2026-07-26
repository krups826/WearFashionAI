package com.virtualtryon.Exception;

public class DuplicateEmailException extends RuntimeException{

    public DuplicateEmailException(String message){
        super(message);
    }

}
