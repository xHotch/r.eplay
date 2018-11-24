package at.ac.tuwien.sepm.assignment.group.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThreadPoolConfig {

    @Value("${application.pool.size}")
    private int poolSize;

    @Bean
    public ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(5);
    }
}
