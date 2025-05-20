package nova.mjs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

//절대 바꾸면 안 됨(이름을) - 모든 프로젝트의 근간이 되는 class
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MjsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MjsApplication.class, args);
    }

}