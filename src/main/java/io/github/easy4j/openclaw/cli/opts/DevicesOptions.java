package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.util.OpenClawLists;
import io.github.easy4j.openclaw.util.OpenClawStrings;
import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw devices}：审批移动/桌面控制端等设备配对请求，并管理设备作用域 token（轮换与吊销）。
 * <p>设置 {@code --url} 时须显式提供 {@code --token} 或 {@code --password}，CLI 不会回退到配置文件或环境变量。
 * 多数操作需要 {@code operator.pairing} 或 {@code operator.admin} 等作用域。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/devices">devices CLI</a>
 */
public final class DevicesOptions implements CliSubArgs {

    /**
     * devices 子命令：列出、移除、批量清理、审批、拒绝、轮换或吊销设备 token。
     */
    public enum Verb {
        /** {@code devices list}：待配对与已配对设备表。 */
        LIST,
        /** {@code devices remove}：删除单条已配对记录。 */
        REMOVE,
        /** {@code devices clear}：批量清理（必须 {@code --yes}）。 */
        CLEAR,
        /** {@code devices approve}：批准待处理配对（可省略 id 以取最新）。 */
        APPROVE,
        /** {@code devices reject}：拒绝指定请求。 */
        REJECT,
        /** {@code devices rotate}：为既有角色轮换设备 token（可更新 scope 集合）。 */
        ROTATE,
        /** {@code devices revoke}：吊销某设备某角色的 token。 */
        REVOKE
    }

    /** list / remove / clear / approve / reject / rotate / revoke 之一。 */
    private final Verb verb;
    /**
     * remove / rotate / revoke：设备 id（{@code --device} 或位置参数，依 Builder 实现）。
     */
    private final String deviceId;
    /**
     * clear：{@code --yes} 必填门闩，防止误删。
     */
    private final boolean clearYes;
    /**
     * clear：{@code --pending} 同时清理待处理请求。
     */
    private final boolean clearPending;
    /**
     * approve / reject：配对请求 id；approve 可与 {@code approveLatest} 互斥组合见文档。
     */
    private final String requestId;
    /**
     * approve：{@code --latest} 显式选择最新待处理请求。
     */
    private final boolean approveLatest;
    /**
     * rotate / revoke：目标角色名（必须是该设备已批准契约内的角色）。
     */
    private final String role;
    /**
     * rotate：重复 {@code --scope} 累加的 operator scope；省略则沿用缓存批准集合。
     */
    private final List<String> scopes;
    /**
     * 全局：{@code --url} Gateway WebSocket（与 gateway 查询命令共享「显式 url 不回退凭据」规则）。
     */
    private final String url;
    /**
     * 全局：{@code --token} 网关共享 token。
     */
    private final String token;
    /**
     * 全局：{@code --password} 网关密码认证。
     */
    private final String password;
    /**
     * 全局：{@code --timeout} RPC 超时。
     */
    private final String timeout;
    /**
     * 全局：{@code --json} 建议脚本使用。
     */
    private final boolean json;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private DevicesOptions(Builder b) {
        this.verb = b.verb;
        this.deviceId = b.deviceId;
        this.clearYes = b.clearYes;
        this.clearPending = b.clearPending;
        this.requestId = b.requestId;
        this.approveLatest = b.approveLatest;
        this.role = b.role;
        this.scopes = b.scopes == null ? OpenClawLists.empty() : OpenClawLists.copyOf(b.scopes);
        this.url = b.url;
        this.token = b.token;
        this.password = b.password;
        this.timeout = b.timeout;
        this.json = b.json;
        this.extra = b.extra == null ? OpenClawLists.empty() : OpenClawLists.copyOf(b.extra);
    }

    /**
     * @return 新 {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> toSubcommandArguments() {
        List<String> out = new ArrayList<>();
        switch (verb) {
            case LIST:
                out.add("list");
                break;
            case REMOVE:
                out.add("remove");
                if (deviceId != null && OpenClawStrings.isNotBlank(deviceId)) {
                    out.add(deviceId.trim());
                }
                break;
            case CLEAR:
                out.add("clear");
                OpenClawCliArgv.addFlag(out, "--yes", clearYes);
                OpenClawCliArgv.addFlag(out, "--pending", clearPending);
                break;
            case APPROVE:
                out.add("approve");
                if (approveLatest) {
                    out.add("--latest");
                } else if (requestId != null && OpenClawStrings.isNotBlank(requestId)) {
                    out.add(requestId.trim());
                }
                break;
            case REJECT:
                out.add("reject");
                if (requestId != null && OpenClawStrings.isNotBlank(requestId)) {
                    out.add(requestId.trim());
                }
                break;
            case ROTATE:
                out.add("rotate");
                OpenClawCliArgv.addIfPresent(out, "--device", deviceId);
                OpenClawCliArgv.addIfPresent(out, "--role", role);
                for (String s : scopes) {
                    if (s != null && OpenClawStrings.isNotBlank(s)) {
                        out.add("--scope");
                        out.add(s.trim());
                    }
                }
                break;
            case REVOKE:
                out.add("revoke");
                OpenClawCliArgv.addIfPresent(out, "--device", deviceId);
                OpenClawCliArgv.addIfPresent(out, "--role", role);
                break;
            default:
                break;
        }
        OpenClawCliArgv.addIfPresent(out, "--url", url);
        OpenClawCliArgv.addIfPresent(out, "--token", token);
        OpenClawCliArgv.addIfPresent(out, "--password", password);
        OpenClawCliArgv.addIfPresent(out, "--timeout", timeout);
        OpenClawCliArgv.addFlag(out, "--json", json);
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link DevicesOptions} 构建器。
     */
    public static final class Builder {
        private Verb verb = Verb.LIST;
        private String deviceId;
        private boolean clearYes;
        private boolean clearPending;
        private String requestId;
        private boolean approveLatest;
        private String role;
        private List<String> scopes = new ArrayList<>();
        private String url;
        private String token;
        private String password;
        private String timeout;
        private boolean json;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}（{@code devices list}）
         */
        public Builder list() {
            this.verb = Verb.LIST;
            return this;
        }

        /**
         * @param deviceId 设备 ID
         * @return {@code this}
         */
        public Builder remove(String deviceId) {
            this.verb = Verb.REMOVE;
            this.deviceId = deviceId;
            return this;
        }

        /**
         * @param yes clear：{@code --yes}
         * @return {@code this}
         */
        public Builder clear(boolean yes) {
            this.verb = Verb.CLEAR;
            this.clearYes = yes;
            return this;
        }

        /**
         * @param pending clear：{@code --pending}
         * @return {@code this}
         */
        public Builder clearPending(boolean pending) {
            this.clearPending = pending;
            return this;
        }

        /**
         * @return {@code this}（无参数 approve）
         */
        public Builder approve() {
            this.verb = Verb.APPROVE;
            this.requestId = null;
            this.approveLatest = false;
            return this;
        }

        /**
         * @param requestId 请求 ID
         * @return {@code this}
         */
        public Builder approve(String requestId) {
            this.verb = Verb.APPROVE;
            this.requestId = requestId;
            this.approveLatest = false;
            return this;
        }

        /**
         * @param latest {@code --latest}
         * @return {@code this}
         */
        public Builder approveLatest(boolean latest) {
            this.approveLatest = latest;
            return this;
        }

        /**
         * @param requestId 请求 ID
         * @return {@code this}
         */
        public Builder reject(String requestId) {
            this.verb = Verb.REJECT;
            this.requestId = requestId;
            return this;
        }

        /**
         * @param deviceId {@code --device}
         * @param role {@code --role}
         * @return {@code this}
         */
        public Builder rotate(String deviceId, String role) {
            this.verb = Verb.ROTATE;
            this.deviceId = deviceId;
            this.role = role;
            return this;
        }

        /**
         * @param scope rotate：追加 {@code --scope}
         * @return {@code this}
         */
        public Builder scope(String scope) {
            if (scope != null && OpenClawStrings.isNotBlank(scope)) {
                scopes.add(scope.trim());
            }
            return this;
        }

        /**
         * @param deviceId {@code --device}
         * @param role {@code --role}
         * @return {@code this}
         */
        public Builder revoke(String deviceId, String role) {
            this.verb = Verb.REVOKE;
            this.deviceId = deviceId;
            this.role = role;
            return this;
        }

        /**
         * @param url {@code --url}
         * @return {@code this}
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * @param token {@code --token}
         * @return {@code this}
         */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        /**
         * @param password {@code --password}
         * @return {@code this}
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * @param timeout {@code --timeout}
         * @return {@code this}
         */
        public Builder timeout(String timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * @param json {@code --json}
         * @return {@code this}
         */
        public Builder json(boolean json) {
            this.json = json;
            return this;
        }

        /**
         * 追加额外 argv token。
         *
         * @param tokens 可为 null（忽略）
         * @return {@code this}
         */
        public Builder extra(String... tokens) {
            if (tokens != null) {
                Collections.addAll(extra, tokens);
            }
            return this;
        }

        /**
         * @return 不可变 {@link DevicesOptions}
         */
        public DevicesOptions build() {
            return new DevicesOptions(this);
        }
    }
}
