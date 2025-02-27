package com.salas.emailnotificationservice.exseption;

public class NonRetryebleExeception extends RuntimeException {
    public NonRetryebleExeception(String message) {
        super(message);
    }

    public NonRetryebleExeception(Throwable cause) {
        super(cause);
    }
}
