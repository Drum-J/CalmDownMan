package chimhaha.chimcard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ChimCardApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChimCardApplication.class, args);
    }

}
