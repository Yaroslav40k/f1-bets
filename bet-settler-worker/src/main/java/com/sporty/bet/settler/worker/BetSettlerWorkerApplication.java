package com.sporty.bet.settler.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstraps the bet settler worker that consumes settlement work units from RocketMQ.
 */
@SpringBootApplication
public class BetSettlerWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BetSettlerWorkerApplication.class, args);
    }

}
