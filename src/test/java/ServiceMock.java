import org.example.Service;

public class ServiceMock implements Service {
    private Integer upperLimit = 10;

    public void setUpperLimit(Integer upperLimit) {
        this.upperLimit = upperLimit;
    }

    public Integer getUpperLimit() {
        return upperLimit;
    }

    public ServiceMock() {
    }

    @Override
    public void doWork() {
        for (int i = 0; i < upperLimit; i++) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
