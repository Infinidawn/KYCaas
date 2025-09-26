package bw.co.btc.kyc.onboarding.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import liquibase.integration.spring.SpringLiquibase;
import javax.sql.DataSource;
@Configuration
public class LiquibaseConfig {
  @Bean
  public SpringLiquibase liquibase(DataSource dataSource) {
    SpringLiquibase lb = new SpringLiquibase();
    lb.setDataSource(dataSource);
    lb.setChangeLog("classpath:db/changelog/master.xml");
    return lb;
  }
}
