package com.github.hiwepy.openclaw;

/**
 * 调用 OpenClaw Gateway {@code POST /hooks/agent} 的请求体（与官方 Webhook 字段对齐）。
 */
public class InvokeAgentRequest {

    private String message;
    private String agentId;
    private String name = "Generation";
    private String wakeMode = "now";
    private int timeoutSeconds = 300;

    public InvokeAgentRequest() {
    }

    public InvokeAgentRequest(String agentId, String message) {
        this.agentId = agentId;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWakeMode() {
        return wakeMode;
    }

    public void setWakeMode(String wakeMode) {
        this.wakeMode = wakeMode;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
