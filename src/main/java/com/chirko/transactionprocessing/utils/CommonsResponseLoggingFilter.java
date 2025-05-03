package com.chirko.transactionprocessing.utils;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonsResponseLoggingFilter extends AbstractResponseLoggingFilter {

    private final Logger logger = LoggerFactory.getLogger(CommonsResponseLoggingFilter.class);

    @Override
    protected void beforeResponse(HttpServletResponse response, String message) {
        logger.info(message);
    }
}
