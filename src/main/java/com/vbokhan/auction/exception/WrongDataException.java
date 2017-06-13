package com.vbokhan.auction.exception;

/**
 * Created by vbokh on 07.06.2017.
 */
public class WrongDataException extends Exception {
    public WrongDataException() {
        super();
    }

    public WrongDataException(String message) {
        super(message);
    }

    public WrongDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongDataException(Throwable cause) {
        super(cause);
    }
}
