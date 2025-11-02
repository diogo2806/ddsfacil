package br.com.ddsfacil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DdsFacilAplicacao {

    public static void main(String[] args) {
        SpringApplication.run(DdsFacilAplicacao.class, args);
    }
}
