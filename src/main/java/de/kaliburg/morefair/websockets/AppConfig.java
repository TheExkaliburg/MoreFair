package de.kaliburg.morefair.websockets;

import com.github.javafaker.ChuckNorris;
import com.github.javafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public ChuckNorris chuckNorris() {
        return (new Faker()).chuckNorris();
    }
}
