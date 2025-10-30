package org.winex.platform;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WinApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(WinApplication.class, args);
        HazelcastInstance hz = ctx.getBean(HazelcastInstance.class);

        System.out.println("==Hazelcast node started==: " + hz.getName());
        System.out.println("==Cluster members==: " + hz.getCluster().getMembers());
    }
}