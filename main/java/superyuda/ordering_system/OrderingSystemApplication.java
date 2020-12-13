package superyuda.ordering_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import superyuda.ordering_system.jobs.DispatchJob;

@SpringBootApplication
public class OrderingSystemApplication {

    public static void main(String[] args) {
        ApplicationContext context =
                SpringApplication
                        .run(OrderingSystemApplication.class, args);
        DispatchJob dispatchJob = (DispatchJob) context.getBean("dispatchJob");
        Thread job = new Thread(dispatchJob);
        job.start();
    }
}

