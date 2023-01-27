package org.example;

import javax.swing.*;

public class Response {
    private State state;

    private Integer failureCount;

    private ResponseCode responseCode;

    public void setState(State state) {
        this.state = state;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public void setResponseCode(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String toString() {
        return "Response{" +
                "state=" + state +
                ", failureCount=" + failureCount +
                ", responseCode=" + responseCode +
                '}';
    }

    public Integer getFailureCount() {
        return failureCount;
    }
}
