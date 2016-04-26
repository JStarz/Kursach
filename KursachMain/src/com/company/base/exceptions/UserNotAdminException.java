package com.company.base.exceptions;

public class UserNotAdminException extends Exception {
    public UserNotAdminException() { super(); }
    public UserNotAdminException(String message) { super(message); }
    public UserNotAdminException(String message, Throwable cause) { super(message, cause); }
    public UserNotAdminException(Throwable cause) { super(cause); }
}
