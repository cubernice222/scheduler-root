package org.carrot.scheduler.center.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"classpath:dubbo/dubbo-provider.xml"})
public class DubboConfig {

}
