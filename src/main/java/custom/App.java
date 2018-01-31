package custom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@ServletComponentScan
@SpringBootApplication
public class App extends SpringBootServletInitializer {
	public static void main(String[] args) {

		SpringApplication.run(App.class, args);
	}

}


