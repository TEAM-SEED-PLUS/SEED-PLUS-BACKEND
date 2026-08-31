package seed.seedplusbackend;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider", modifyOnCreate = false)
@ConfigurationPropertiesScan
@SpringBootApplication
public class SeedPlusBackendApplication {

  public static void main(String[] args) {

    System.setProperty("user.timezone", "Asia/Seoul");
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    SpringApplication.run(SeedPlusBackendApplication.class, args);
  }
}
