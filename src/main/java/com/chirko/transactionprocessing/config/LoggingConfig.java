package com.chirko.transactionprocessing.config;

import com.chirko.transactionprocessing.utils.CommonsResponseLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class LoggingConfig {

    @Bean
    public FilterRegistrationBean<CommonsRequestLoggingFilter> requestLoggingFilterFilterRegistrationBean() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludeHeaders(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);

        FilterRegistrationBean<CommonsRequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(2);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<CommonsResponseLoggingFilter> responseLoggingFilterFilterRegistrationBean() {
        CommonsResponseLoggingFilter filter = new CommonsResponseLoggingFilter();
        filter.setIncludeHeaders(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);

        FilterRegistrationBean<CommonsResponseLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(1);
        return registration;
    }

}
