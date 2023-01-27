package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Driver {
    private final Service service;

    private final FooProxy proxy;

    Driver() {
        service = new ServiceA();
        proxy = FooProxy.create();
    }

    public void sendRequest(int numOfRequest) {
        ExecutorService executorService = Executors.newFixedThreadPool(numOfRequest);
        List<Future<Response>> futures = new ArrayList<>();

        for (int i = 0; i < numOfRequest; i++) {
//            Response response = new FooProxy().invoke(service);
//            System.out.println(response);
//            try {
//                Thread.sleep(2);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            futures.add(executorService.submit(() -> proxy.invoke(service)));
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        futures.forEach((future) -> {
            try {
                System.out.println(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        executorService.shutdown();
    }
}
