package org.winex.collector;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.winex.hazelcast.common.config.HazelcastConfiguration;

@SpringBootApplication
@Import(HazelcastConfiguration.class)
public class CollectorApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(CollectorApplication.class, args);
        HazelcastInstance hz = ctx.getBean(HazelcastInstance.class);

        System.out.println("==Hazelcast node started==: " + hz.getName());
        System.out.println("==Cluster members==: " + hz.getCluster().getMembers());
    }

}
