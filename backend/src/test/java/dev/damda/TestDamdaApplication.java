package dev.damda;

import org.springframework.boot.SpringApplication;

public class TestDamdaApplication {

    public static void main(String[] args) {
        SpringApplication.from(DamdaApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
