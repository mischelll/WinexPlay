package org.winex.hazelcast.common.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.winex.hazelcast.common.constants.HazelcastConstants.*;

@Configuration
public class HazelcastConfiguration {

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();

        config.setClusterName(CLUSTER_NAME);

        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5701).setPortAutoIncrement(true);

        JoinConfig join = network.getJoin();
        join.getTcpIpConfig().setEnabled(false);
        join.getMulticastConfig()
                .setEnabled(true)
                .setMulticastGroup("224.2.2.3")
                .setMulticastPort(54327);

        config.getMapConfig(MAP_PLAYER_SCORES).setBackupCount(1);
        config.getMapConfig(MAP_PLAYER_BETS).setBackupCount(1);
        config.getMapConfig(MAP_MARKETS).setBackupCount(1);

        return config;
    }

    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance hazelcastInstance(Config config) {
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        System.out.println("🚀 Hazelcast instance started: " + instance.getName());
        return instance;
    }
}
