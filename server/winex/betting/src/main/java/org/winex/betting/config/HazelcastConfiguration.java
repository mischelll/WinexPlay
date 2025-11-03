package org.winex.betting.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.winex.betting.event.Bet;

@Configuration
public class HazelcastConfiguration {

    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.setClusterName("igaming-cluster");

        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5701).setPortAutoIncrement(true);

        JoinConfig join = network.getJoin();
        join.getTcpIpConfig().setEnabled(false);
        join.getMulticastConfig()
                .setEnabled(true)
                .setMulticastGroup("224.2.2.3")
                .setMulticastPort(54327);

        config.getMapConfig("liveStats")
                .setBackupCount(1)
                .setTimeToLiveSeconds(0);

        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean
    public ITopic<Bet> betTopic(HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getTopic("bets");
    }
}
