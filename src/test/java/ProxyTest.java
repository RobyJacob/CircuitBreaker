import org.example.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProxyTest {
    FooProxy proxy = FooProxy.create();
    ServiceMock serviceMock = new ServiceMock();

    @Test
    public void testSingletonObject() {
        FooProxy proxy1 = FooProxy.create();

        Assertions.assertEquals(proxy, proxy1);
    }

    @Test
    public void testDefaultState() {
        proxy.resetState(Environment.TEST);

        Assertions.assertEquals(State.CLOSED, proxy.getState());
    }

    @Test
    public void testDefaultFailureCount() {
        proxy.resetFailureCount(Environment.TEST);

        Assertions.assertEquals(0, proxy.getFailureCount());
    }

    @Test
    public void testDefaultThreshold() {
        Assertions.assertEquals(3, proxy.getThreshold());
    }

    @Test
    public void testDefaultTimeout() {
        Assertions.assertEquals(5L, proxy.getTimeout());
    }

    @Test
    public void testDefaultWaitTime() {
        Assertions.assertEquals(100L, proxy.getWaitTime());
    }

    @Test
    public void testDefaultLastFailureTime() {
        proxy.resetLastFailureTime(Environment.TEST);

        Assertions.assertEquals(Long.MAX_VALUE, proxy.getLastFailureTime());
    }

    @Test
    public void testEvaluateStateWithDefaultValues() {
        proxy.resetFailureCount(Environment.TEST);
        proxy.resetState(Environment.TEST);
        proxy.resetLastFailureTime(Environment.TEST);
        proxy.evaluateState();

        Assertions.assertEquals(State.CLOSED, proxy.getState());
    }

    @Test
    public void testEvaluateStateWithNonDefaultFailureCount() {
        proxy.setFailureCount(Environment.TEST, 2);
        proxy.evaluateState();

        Assertions.assertEquals(State.CLOSED, proxy.getState());

        proxy.setFailureCount(Environment.TEST, 3);
        proxy.evaluateState();

        Assertions.assertEquals(State.CLOSED, proxy.getState());

        proxy.setFailureCount(Environment.TEST, 4);
        proxy.evaluateState();

        Assertions.assertEquals(State.OPEN, proxy.getState());
    }

    @Test
    public void testSetFailureCount() {
        proxy.setFailureCount(Environment.DEV, 2);

        Assertions.assertEquals(0, proxy.getFailureCount());

        proxy.setFailureCount(Environment.TEST, 3);

        Assertions.assertEquals(3, proxy.getFailureCount());
    }

    @Test
    public void testEvaluateStateWithNonDefaultLastFailureTimeAndFailureCount() {
        proxy.setLastFailureTime(Environment.TEST, 0L);
        proxy.setFailureCount(Environment.TEST, 4);
        proxy.evaluateState();

        Assertions.assertEquals(State.PARTIAL_OPEN, proxy.getState());

        proxy.setLastFailureTime(Environment.TEST, System.currentTimeMillis() + 10);
        proxy.evaluateState();

        Assertions.assertEquals(State.OPEN, proxy.getState());
    }

    @Test
    public void testEvaluateState_whenStateOpen_thenLastFailureTime_shouldNotUpdate() {
        proxy.setState(Environment.TEST, State.OPEN);
        proxy.setLastFailureTime(Environment.TEST, 1000L);
        proxy.setFailureCount(Environment.TEST, 4);
        proxy.evaluateState();

        Assertions.assertEquals(1000L, proxy.getLastFailureTime());
    }

    @Test
    public void testCalculateExecutionTime() {
        Long executionTime = proxy.getExecutionTime(null);

        Assertions.assertEquals(0L, executionTime);

        serviceMock.setUpperLimit(10);
        executionTime = proxy.getExecutionTime(serviceMock);

        Assertions.assertEquals(11L, executionTime);
    }

    @Test
    public void testInvokeSuccess() {
        serviceMock.setUpperLimit(4);

        proxy.resetState(Environment.TEST);
        proxy.resetFailureCount(Environment.TEST);
        proxy.resetLastFailureTime(Environment.TEST);

        Response response = proxy.invoke(serviceMock);

        Response expResponse = new Response();
        expResponse.setResponseCode(ResponseCode.SUCCESS);
        expResponse.setFailureCount(0);
        expResponse.setState(State.CLOSED);

        Assertions.assertEquals(expResponse.toString(), response.toString());
    }

    @Test
    public void testInvokeTimeout() {
        serviceMock.setUpperLimit(10);

        proxy.resetState(Environment.TEST);
        proxy.resetFailureCount(Environment.TEST);
        proxy.resetLastFailureTime(Environment.TEST);

        Response response = proxy.invoke(serviceMock);

        Response expResponse = new Response();
        expResponse.setResponseCode(ResponseCode.TIMEOUT);
        expResponse.setFailureCount(1);
        expResponse.setState(State.CLOSED);

        Assertions.assertEquals(expResponse.toString(), response.toString());
    }

    @Test
    public void testInvokeFailure() {
        serviceMock.setUpperLimit(10);

        proxy.setLastFailureTime(Environment.TEST, System.currentTimeMillis());
        proxy.setFailureCount(Environment.TEST, 4);
        proxy.setState(Environment.TEST, State.OPEN);

        Response response = proxy.invoke(serviceMock);

        Response expResponse = new Response();

        expResponse.setFailureCount(4);
        expResponse.setState(State.OPEN);
        expResponse.setResponseCode(ResponseCode.FAILURE);

        Assertions.assertEquals(expResponse.toString(), response.toString());

    }

    @Test
    public void testInvokePartialOpenState() {
        serviceMock.setUpperLimit(10);

        proxy.setLastFailureTime(Environment.TEST, System.currentTimeMillis() -
                proxy.getWaitTime() * 2);
        proxy.setFailureCount(Environment.TEST, proxy.getThreshold() + 1);
        proxy.setState(Environment.TEST, State.OPEN);

        Response response = proxy.invoke(serviceMock);

        Response expResponse = new Response();

        expResponse.setFailureCount(4);
        expResponse.setState(State.PARTIAL_OPEN);
        expResponse.setResponseCode(ResponseCode.FAILURE);

        Assertions.assertEquals(expResponse.toString(), response.toString());
    }
}
