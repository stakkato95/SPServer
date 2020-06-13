package com.stakkato95.service.drone.rest;

public class RestResponse<T> {
    public boolean successful;
    public String message;
    public T payload;
}
