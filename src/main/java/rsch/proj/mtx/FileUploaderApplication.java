package rsch.proj.mtx;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import rsch.proj.mtx.properties.StorageProperties;
import rsch.proj.mtx.service.StorageService;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class FileUploaderApplication{

	public static void main(String[] args) {
		SpringApplication.run(FileUploaderApplication.class, args);
	}
	
	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			storageService.deleteAll();
			storageService.init();
		};
	}
}
