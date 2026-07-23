package io.github.easy4j.openclaw.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Tools Invoke API 请求体。
 * <p>
 * 对应 {@code POST /tools/invoke} 的请求 JSON。
 * 此端点始终启用，使用 Gateway 鉴权 + 工具策略。
 * 共享密钥鉴权（token/password）被视为受信任的 operator 访问。
 * </p>
 *
 * <h3>工具可用性</h3>
 * <p>工具通过以下策略链过滤：</p>
 * <ul>
 *   <li>{@code tools.profile} / {@code tools.byProvider.profile}</li>
 *   <li>{@code tools.allow} / {@code tools.byProvider.allow}</li>
 *   <li>{@code agents.<id>.tools.allow} / {@code agents.<id>.tools.byProvider.allow}</li>
 *   <li>组策略（当 session key 映射到组或通道时）</li>
 *   <li>子 agent 策略</li>
 * </ul>
 * <p>若工具不被策略允许，端点返回 {@code 404}。</p>
 *
 * <h3>默认拒绝列表</h3>
 * <p>即使 session 策略允许，Gateway HTTP 也默认拒绝以下工具：</p>
 * <ul>
 *   <li>{@code exec}、{@code spawn}、{@code shell} - RCE 表面</li>
 *   <li>{@code fs_write}、{@code fs_delete}、{@code fs_move} - 文件系统写操作</li>
 *   <li>{@code apply_patch} - 补丁应用可重写任意文件</li>
 *   <li>{@code sessions_spawn}、{@code sessions_send} - 会话编排</li>
 *   <li>{@code cron} - 持久自动化控制平面</li>
 *   <li>{@code gateway} - Gateway 控制平面</li>
 *   <li>{@code nodes} - 节点命令中继</li>
 *   <li>{@code whatsapp_login} - 交互式设置</li>
 * </ul>
 * <p>可通过 {@code gateway.tools.deny} 和 {@code gateway.tools.allow} 自定义此列表。</p>
 *
 * @see <a href="https://docs.openclaw.ai/gateway/tools-invoke-http-api">Tools Invoke API</a>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolInvokeRequest {

    /**
     * 工具名称（必填）。
     * <p>如 {@code "sessions_list"}、{@code "browser"} 等。</p>
     */
    private String tool;

    /**
     * 操作名称（可选）。
     * <p>若工具 schema 支持 {@code action} 且 args 中未包含，则映射到 args 中。</p>
     */
    private String action;

    /**
     * 工具特定参数（可选）。
     * <p>工具专用的键值对参数。</p>
     */
    private Map<String, Object> args;

    /**
     * 目标 session key（可选）。
     * <p>若省略或为 {@code "main"}，Gateway 使用配置的主 session key
     * （遵循 {@code session.mainKey} 和默认 agent，或 global scope 下的 {@code "global"}）。</p>
     */
    private String sessionKey;

    /**
     * 干运行模式（可选，预留未来使用）。
     * <p>当前被忽略。</p>
     */
    private Boolean dryRun;
}
