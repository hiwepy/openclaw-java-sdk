package com.github.hiwepy.openclaw;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OpenClawGatewayHttpClient} 响应解析单元测试（无需真实 Gateway）。
 */
class OpenClawGatewayHttpClientTest {

    @Test
    void parseOk_extractsBooleanFromJson() {
        assertTrue(OpenClawGatewayHttpClient.parseOk("{\"ok\":true,\"runId\":\"x\"}"));
        assertFalse(OpenClawGatewayHttpClient.parseOk("{\"ok\":false}"));
    }

    @Test
    void parseOk_fallbackToSubstring() {
        assertTrue(OpenClawGatewayHttpClient.parseOk("not-json but \"ok\":true here"));
    }

    @Test
    void parseRunId_readsField() {
        assertEquals("2795185c-cb1c-4b43-b27d-87496e78cb87",
                OpenClawGatewayHttpClient.parseRunId(
                        "{\"ok\":true,\"runId\":\"2795185c-cb1c-4b43-b27d-87496e78cb87\"}"));
    }

    @Test
    void normalizeHookName_acceptsSimpleNameAndHooksPrefix() {
        assertEquals("gmail", OpenClawGatewayHttpClient.normalizeHookName("gmail"));
        assertEquals("gmail", OpenClawGatewayHttpClient.normalizeHookName("hooks/gmail"));
        assertEquals("gmail", OpenClawGatewayHttpClient.normalizeHookName("/hooks/gmail"));
    }

    @Test
    void normalizeHookName_rejectsIllegalPathTraversal() {
        assertThrows(IllegalArgumentException.class,
                () -> OpenClawGatewayHttpClient.normalizeHookName("../wake"));
    }
}
