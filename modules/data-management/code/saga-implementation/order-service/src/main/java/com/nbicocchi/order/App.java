package com.nbicocchi.order;

import com.nbicocchi.order.persistence.repository.ProductRepository;
import com.nbicocchi.order.worker.PurchaseInsertWorker;
import com.netflix.conductor.client.automator.TaskRunnerConfigurer;
import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.client.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class App implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    private final ProductRepository productPurchaseRepository;

    public App(ProductRepository productPurchaseRepository) {
        this.productPurchaseRepository = productPurchaseRepository;
    }

    public static void main(final String... args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        TaskClient taskClient = new TaskClient();
        taskClient.setRootURI("http://localhost:5000/api/"); // Point this to the server API

        Worker worker1 = new PurchaseInsertWorker("product_sale", productPurchaseRepository);
        List<Worker> workerArrayList = new ArrayList<>(List.of(worker1));

        // Start the polling and execution of tasks
        int threadCount = 1; // number of threads used to execute workers.  To avoid starvation, should be
        TaskRunnerConfigurer configurer =
                new TaskRunnerConfigurer.Builder(taskClient, workerArrayList)
                        .withThreadCount(threadCount)
                        .build();
        configurer.init();
    }

}
