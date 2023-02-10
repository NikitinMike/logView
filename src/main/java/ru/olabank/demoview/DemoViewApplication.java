package ru.olabank.demoview;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class DemoViewApplication {

    public static void main(String[] args) throws IOException, GeoIp2Exception {
        SpringApplication.run(DemoViewApplication.class, args);
    }

}
