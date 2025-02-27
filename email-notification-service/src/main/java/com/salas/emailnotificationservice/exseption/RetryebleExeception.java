package com.salas.emailnotificationservice.exseption;

public class RetryebleExeception extends RuntimeException {
    public RetryebleExeception(String message) {
        super(message);
    }

    public RetryebleExeception(Throwable cause) {
        super(cause);
    }
}
