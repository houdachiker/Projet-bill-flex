package com.billing.facturation.exception;

/** V6 : un service distant (appele via Feign) est indisponible ou le circuit breaker est ouvert -> HTTP 503. */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) { super(message); }
    public ServiceUnavailableException(String message, Throwable cause) { super(message, cause); }
}
