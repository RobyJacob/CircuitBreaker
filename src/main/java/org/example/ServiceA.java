package org.example;

public class ServiceA implements Service {
    @Override
    public void doWork() {
        for (int i = 0; i < 1 + Math.random() * 10; i++) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
