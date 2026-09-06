package com.alkateca.lendasdapoeira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LendasDaPoeiraApplication {

    public static void main(String[] args) {
        // Isso liga o servidor Tomcat na porta 8080
        SpringApplication.run(LendasDaPoeiraApplication.class, args);
        System.out.println(">>> Servidor do Jogo Rodando na porta 8080! <<<");
    }
}