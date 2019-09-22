package mouamle.tggh;

import mouamle.tggh.common.ServiceInterface;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Bean
    ApplicationRunner init(@Qualifier("githubInterface") ServiceInterface githubInterface,
                           @Qualifier("telegramInterface") ServiceInterface telegramInterface) {
        return args -> {
            githubInterface.init();
            telegramInterface.init();
        };
    }
}
