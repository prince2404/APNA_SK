package com.ask.security;

import com.ask.constants.ApiPaths;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthRateLimitFilterTest {

    @Test
    void limitsPublicAuthEndpointsByClientIpAndPath() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        AuthRateLimitFilter filter = new AuthRateLimitFilter(objectMapper);
        ReflectionTestUtils.setField(filter, "capacity", 1L);
        ReflectionTestUtils.setField(filter, "refillMinutes", 1L);

        MockHttpServletRequest firstRequest = request();
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(firstRequest, firstResponse, new MockFilterChain());

        MockHttpServletRequest secondRequest = request();
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(secondRequest, secondResponse, new MockFilterChain());

        assertEquals(200, firstResponse.getStatus());
        assertEquals(429, secondResponse.getStatus());
        assertTrue(Long.parseLong(secondResponse.getHeader("Retry-After")) > 0);
        assertTrue(secondResponse.getContentAsString().contains("RATE_LIMIT_EXCEEDED"));
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ApiPaths.AUTH + ApiPaths.AUTH_LOGIN);
        request.setServletPath(ApiPaths.AUTH + ApiPaths.AUTH_LOGIN);
        request.setRemoteAddr("127.0.0.1");
        return request;
    }
}
