package com.activiti.config.activiti;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.stereotype.Component;

/**
 * 图片乱码
 */
@Component
public class activitiConfig implements ProcessEngineConfigurationConfigurer {

        @Override
        public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
            processEngineConfiguration.setActivityFontName("WenQuanYi Zen Hei");
        }
}
