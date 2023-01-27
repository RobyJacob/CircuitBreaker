package org.example;

public class FooProxy {
    private static State state;

    private final Integer THRESHOLD;

    private final Long TIMEOUT;

    private final Long WAIT_TIME;

    private static Integer failureCount;

    private Long lastFailureTime;

    private ResponseCode responseCode;

    private static FooProxy proxy;

    public static FooProxy create() {
        if (proxy == null) {
            proxy = new FooProxy();
        }

        return proxy;
    }

    private FooProxy() {
        THRESHOLD = 3;
        TIMEOUT = 5L;
        WAIT_TIME = 100L;
        lastFailureTime = Long.MAX_VALUE;
        responseCode = ResponseCode.SUCCESS;
    }

    static {
        state = State.CLOSED;
        failureCount = 0;
    }

    public void evaluateState() {
        if (failureCount > THRESHOLD) {
            if (System.currentTimeMillis() - lastFailureTime > WAIT_TIME) state = State.PARTIAL_OPEN;
            else {
                if (state != State.OPEN) lastFailureTime = System.currentTimeMillis();
                state = State.OPEN;
            }
        } else state = State.CLOSED;
    }

    public Long getExecutionTime(Service service) {
        long startTime = System.currentTimeMillis();

        if (service != null) service.doWork();

        return System.currentTimeMillis() - startTime;
    }

    public synchronized Response invoke(Service service) {
        evaluateState();

        switch (state) {
            case CLOSED:
                Long executionTime = getExecutionTime(service);
                if (executionTime > TIMEOUT) {
                    failureCount++;
                    responseCode = ResponseCode.TIMEOUT;
                } else  {
                    responseCode = ResponseCode.SUCCESS;
                }
                break;
            case PARTIAL_OPEN:
                executionTime = getExecutionTime(service);
                if (executionTime > TIMEOUT) {
                    responseCode = ResponseCode.FAILURE;
                    lastFailureTime = System.currentTimeMillis();
                } else {
                    responseCode = ResponseCode.SUCCESS;
                    failureCount = 0;
                    lastFailureTime = Long.MAX_VALUE;
                }
                break;
            default:
                responseCode = ResponseCode.FAILURE;
        }

        Response response = new Response();

        response.setState(state);
        response.setResponseCode(responseCode);
        response.setFailureCount(failureCount);

        return response;
    }

    public State getState() {
        return state;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public Integer getThreshold() {
        return THRESHOLD;
    }

    public Long getTimeout() {
        return TIMEOUT;
    }

    public Long getWaitTime() {
        return WAIT_TIME;
    }

    public void setFailureCount(Environment env, int newFailureCount) {
        if (env == Environment.TEST) failureCount = newFailureCount;
    }

    public void resetState(Environment env) {
        if (Environment.TEST == env) state = State.CLOSED;
    }

    public void resetFailureCount(Environment env) {
        if (Environment.TEST == env) failureCount = 0;
    }

    public Long getLastFailureTime() {
        return lastFailureTime;
    }

    public void setLastFailureTime(Environment env, Long newLastFailureTime) {
        if (Environment.TEST == env) lastFailureTime = newLastFailureTime;
    }

    public void setState(Environment env, State newState) {
        if (Environment.TEST == env) state = newState;
    }

    public void resetLastFailureTime(Environment env) {
        if (Environment.TEST == env) lastFailureTime = Long.MAX_VALUE;
    }
}
