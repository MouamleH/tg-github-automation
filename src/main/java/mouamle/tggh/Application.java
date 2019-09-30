package mouamle.tggh;

import mouamle.tggh.common.ServiceInterface;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    /**
     * Initialize all the services
     */
    @Bean
    ApplicationRunner initServices(List<ServiceInterface> interfaces) {
        return args -> interfaces.forEach(ServiceInterface::init);
    }

}
