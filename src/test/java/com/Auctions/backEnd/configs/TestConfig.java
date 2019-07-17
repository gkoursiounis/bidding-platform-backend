package com.Auctions.backEnd.configs;

import com.Auctions.backEnd.TestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("defaultAutowire = Autowire.BY_TYPE, defaultLazy = Lazy.FALSE")
public class TestConfig {

    @Bean
    public TestUtils testUtils() {
        return new TestUtils();
    }
}
