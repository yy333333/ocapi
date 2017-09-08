package com.sgcc.nrxt.i6000.tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Configuration
public class OcApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(OcApiApplication.class, args);
	}
}
