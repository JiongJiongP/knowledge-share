package com.company.social;

import com.company.common.config.MyBatisPlusConfig;
import com.company.common.config.Sm4Config;
import com.company.social.infrastructure.mq.RabbitMqConfig;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.company.social",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RabbitMqConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        com.company.social.application.service.GroupService.class,
                        com.company.social.interfaces.controller.GroupController.class
                })
        })
@Import({MyBatisPlusConfig.class, Sm4Config.class})
public class TestConfig {
}
