package bw.co.btc.kyc.risk; import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication; import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
@EnableRabbit
public class RiskApplication {
    public static void main(String[] args)
    {
        SpringApplication.run(RiskApplication.class,args);}
}
