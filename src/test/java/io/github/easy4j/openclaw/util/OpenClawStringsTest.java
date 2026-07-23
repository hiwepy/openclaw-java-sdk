package io.github.easy4j.openclaw.util;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenClawStringsTest {

    @Test
    void blankChecks() {
        assertTrue(OpenClawStrings.isBlank(null));
        assertTrue(OpenClawStrings.isBlank("  "));
        assertFalse(OpenClawStrings.isNotBlank("  "));
        assertTrue(OpenClawStrings.isNotBlank(" x "));
    }

    @Test
    void defaultIfBlank() {
        assertEquals("now", OpenClawStrings.defaultIfBlank(null, "now"));
        assertEquals("heartbeat", OpenClawStrings.defaultIfBlank(" heartbeat ", "now"));
    }

    @Test
    void putIfNotBlank() {
        Map<String, Object> body = new LinkedHashMap<>();
        OpenClawStrings.putIfNotBlank(body, "agentId", "  ");
        assertTrue(body.isEmpty());
        OpenClawStrings.putIfNotBlank(body, "agentId", " main ");
        assertEquals("main", body.get("agentId"));
    }

    @Test
    void trimToNull() {
        assertNull(OpenClawStrings.trimToNull(" "));
        assertEquals("a", OpenClawStrings.trimToNull(" a "));
    }
}
