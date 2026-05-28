package com.company.content;

import com.company.common.config.MyBatisPlusConfig;
import com.company.common.config.Sm4Config;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.company.content",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        com.company.content.application.service.ContentService.class,
                        com.company.content.interfaces.controller.ContentController.class
                }))
@Import({MyBatisPlusConfig.class, Sm4Config.class})
public class TestConfig {
}
