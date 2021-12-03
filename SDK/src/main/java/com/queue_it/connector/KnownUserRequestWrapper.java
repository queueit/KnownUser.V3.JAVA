package com.queue_it.connector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


public class KnownUserRequestWrapper extends HttpServletRequestWrapper {

    public KnownUserRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public String GetRequestBodyAsString(){
        return "";
    }
}