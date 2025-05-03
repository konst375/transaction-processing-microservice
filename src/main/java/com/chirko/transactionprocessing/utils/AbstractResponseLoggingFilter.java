package com.chirko.transactionprocessing.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Setter
public abstract class AbstractResponseLoggingFilter extends OncePerRequestFilter {

    private static final String DEFAULT_RESPONSE_LOG_MESSAGE_PREFIX = "Response: [";

    private static final String DEFAULT_RESPONSE_LOG_MESSAGE_SUFFIX = "]";

    private static final int DEFAULT_MAX_PAYLOAD_LENGTH = 50;

    private boolean isIncludePayload = false;

    private boolean isIncludeHeaders = false;

    private int maxPayloadLength = DEFAULT_MAX_PAYLOAD_LENGTH;

    private String messagePrefix = DEFAULT_RESPONSE_LOG_MESSAGE_PREFIX;

    private String messageSuffix = DEFAULT_RESPONSE_LOG_MESSAGE_SUFFIX;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        boolean ifFirstRequest = !isAsyncDispatch(request);
        HttpServletResponse responseToUse = response;

        if (isIncludePayload && ifFirstRequest && !(response instanceof ContentCachingResponseWrapper)) {
            responseToUse = new ContentCachingResponseWrapper(response);
        }

        try {
            filterChain.doFilter(request, responseToUse);
        } finally {
            if (!isAsyncStarted(request)) {
                ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) responseToUse;
                beforeResponse(responseToUse, getResponseLogMessage(responseWrapper, this.messagePrefix, this.messageSuffix));
                responseWrapper.copyBodyToResponse();
            }
        }
    }



    protected String getResponseLogMessage(ContentCachingResponseWrapper response, String prefix, String suffix) {
        StringBuilder msg = new StringBuilder();
        msg.append(prefix);
        msg.append("Status: ").append(response.getStatus()).append(" ")
                .append(HttpStatus.valueOf(response.getStatus()).getReasonPhrase()).append(" ");

        if (isIncludeHeaders) {
            List<String> headerNames = new ArrayList<>(response.getHeaderNames());
            if (!headerNames.isEmpty()) {
                StringBuilder headers = new StringBuilder();

                headerNames.sort(Comparator.naturalOrder());
                headerNames.forEach(headerName -> response.getHeaders(headerName)
                        .forEach(headerValue -> headers.append("> ")
                                .append(headerName).append(": ")
                                .append(headerValue).append(" ")));
                msg.append("Headers: [").append(headers).append("]");
            }
        }

        if (isIncludePayload) {
            final byte[] buf = response.getContentAsByteArray();
            if (buf.length > 0) {
                msg.append("Response body: [");
                int length = Math.min(buf.length, maxPayloadLength);
                try {
                    msg.append(new String(buf, 0, length, response.getCharacterEncoding()));
                } catch (UnsupportedEncodingException ex) {
                    msg.append("[unknown]");
                }
                msg.append("]");
            }
        }

        msg.append(suffix);
        return msg.toString();
    }

    protected abstract void beforeResponse(HttpServletResponse response, String message);
}
