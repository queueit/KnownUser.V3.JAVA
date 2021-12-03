package com.queue_it.connector;

import javax.servlet.http.HttpServletRequest;


public class KnownUserRequestWrapperMock extends KnownUserRequestWrapper {

    String Body = "";
    public KnownUserRequestWrapperMock(HttpServletRequest request) {
        super(request);
    }

    public void SetRequestBodyAsString(String body){
        this.Body = body;
    }

    public String GetRequestBodyAsString(){
        return this.Body;
    }
}