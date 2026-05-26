package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import io.github.hiwepy.openclaw.util.OpenClawStrings;
import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * {@code openclaw agents}：管理相互隔离的 agent（独立 workspace、认证与入站路由绑定）。
 * <p>路由绑定把渠道流量固定到某 agent；技能可见性另由 {@code agents.defaults.skills} 等配置控制。未覆盖子 flag 请用 {@link Builder#extra(String...)}。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/agents">agents CLI</a>
 */
public final class AgentsOptions implements CliSubArgs {

    /**
     * 子命令动词：裸 {@code openclaw agents} 与显式 {@code list} 在 CLI 上等价。
     */
    public enum Verb {
        /**
         * 与 {@code openclaw agents} 不带子命令一致：不发出字面量 {@code list} token，由 Gateway 走默认 list 行为。
         */
        DEFAULT_LIST,
        /**
         * 显式 {@code agents list}。
         */
        LIST,
        /**
         * {@code agents add [name]}：新建隔离 agent。
         */
        ADD,
        /**
         * {@code agents bindings}：列出路由绑定规则。
         */
        BINDINGS,
        /**
         * {@code agents bind}：为某 agent 增加绑定。
         */
        BIND,
        /**
         * {@code agents unbind}：移除绑定或 {@code --all} 清空。
         */
        UNBIND,
        /**
         * {@code agents set-identity}：写入 {@code agents.list[].identity}（名称、主题、emoji、头像等）。
         */
        SET_IDENTITY,
        /**
         * {@code agents delete}：将 workspace 与状态移入废纸篓而非硬删（除非配合 force 与交互规则，见文档）。
         */
        DELETE
    }

    /** 当前 agents 子命令表面。 */
    private final Verb verb;
    /**
     * list / DEFAULT_LIST：{@code --json} 机器可读列表。
     */
    private final boolean listJson;
    /**
     * list：{@code --bindings} 输出完整路由规则，而不只是每 agent 摘要计数。
     */
    private final boolean listBindings;
    /**
     * add：新 agent 的 id 位置参数（{@code main} 为保留字不可用）。
     */
    private final String addName;
    /**
     * add：{@code --workspace} 独立工作区根路径；非交互 add 时与 name 同为必填。
     */
    private final String workspace;
    /**
     * add：{@code --model} 初始默认模型引用。
     */
    private final String model;
    /**
     * add：{@code --agent-dir} 自定义 agent 配置目录。
     */
    private final String agentDir;
    /**
     * add / bind / unbind：可重复 {@code --bind channel:account} 绑定说明（account 省略时按文档解析默认账号）。
     */
    private final List<String> bindValues;
    /**
     * add：{@code --non-interactive} 脚本模式；一旦传入任意 add flag 即进入非交互路径。
     */
    private final boolean nonInteractive;
    /**
     * add：{@code --json} 结构化输出。
     */
    private final boolean addJson;
    /**
     * bindings：{@code --agent} 只查看指定 agent 的绑定。
     */
    private final String bindingsAgent;
    /**
     * bindings：{@code --json}。
     */
    private final boolean bindingsJson;
    /**
     * bind：{@code --agent}，省略时指向当前默认 agent。
     */
    private final String bindAgent;
    /**
     * bind：{@code --json}。
     */
    private final boolean bindJson;
    /**
     * unbind：{@code --agent}，省略时指向当前默认 agent。
     */
    private final String unbindAgent;
    /**
     * unbind：{@code --all} 移除该 agent 全部绑定（与重复 {@code --bind} 互斥）。
     */
    private final boolean unbindAll;
    /**
     * unbind：{@code --json}。
     */
    private final boolean unbindJson;
    /**
     * delete：agent id 位置参数（不可删 {@code main}）。
     */
    private final String deleteAgentId;
    /**
     * delete：{@code --force} 跳过交互确认。
     */
    private final boolean deleteForce;
    /**
     * delete：{@code --json}。
     */
    private final boolean deleteJson;
    /**
     * set-identity：{@code --agent} 与 {@code --workspace} 二选一或组合以定位目标（多 agent 共享 workspace 时必须指定 agent）。
     */
    private final String identityAgent;
    /**
     * set-identity：{@code --workspace} 用于选中 agent 或定位 {@code IDENTITY.md}。
     */
    private final String identityWorkspace;
    /**
     * set-identity：{@code --identity-file} 显式身份文件路径。
     */
    private final String identityFile;
    /**
     * set-identity：{@code --from-identity} 从 workspace 根或 {@code --identity-file} 读取 {@code IDENTITY.md}。
     */
    private final boolean fromIdentity;
    /**
     * set-identity：{@code --name} 显示名。
     */
    private final String identityName;
    /**
     * set-identity：{@code --theme} 主题描述。
     */
    private final String identityTheme;
    /**
     * set-identity：{@code --emoji} 表情符号头像提示。
     */
    private final String identityEmoji;
    /**
     * set-identity：{@code --avatar} 相对 workspace 的路径、http(s) URL 或 data URI。
     */
    private final String identityAvatar;
    /**
     * set-identity：{@code --json}。
     */
    private final boolean identityJson;
    /**
     * 文档新增或未建模 argv，按顺序附加。
     */
    private final List<String> extra;

    private AgentsOptions(Builder b) {
        this.verb = b.verb;
        this.listJson = b.listJson;
        this.listBindings = b.listBindings;
        this.addName = b.addName;
        this.workspace = b.workspace;
        this.model = b.model;
        this.agentDir = b.agentDir;
        this.bindValues = b.bindValues == null ? OpenClawLists.empty() : OpenClawLists.copyOf(b.bindValues);
        this.nonInteractive = b.nonInteractive;
        this.addJson = b.addJson;
        this.bindingsAgent = b.bindingsAgent;
        this.bindingsJson = b.bindingsJson;
        this.bindAgent = b.bindAgent;
        this.bindJson = b.bindJson;
        this.unbindAgent = b.unbindAgent;
        this.unbindAll = b.unbindAll;
        this.unbindJson = b.unbindJson;
        this.deleteAgentId = b.deleteAgentId;
        this.deleteForce = b.deleteForce;
        this.deleteJson = b.deleteJson;
        this.identityAgent = b.identityAgent;
        this.identityWorkspace = b.identityWorkspace;
        this.identityFile = b.identityFile;
        this.fromIdentity = b.fromIdentity;
        this.identityName = b.identityName;
        this.identityTheme = b.identityTheme;
        this.identityEmoji = b.identityEmoji;
        this.identityAvatar = b.identityAvatar;
        this.identityJson = b.identityJson;
        this.extra = b.extra == null ? OpenClawLists.empty() : OpenClawLists.copyOf(b.extra);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<String> toSubcommandArguments() {
        List<String> out = new ArrayList<>();
        switch (verb) {
            case DEFAULT_LIST:
                break;
            case LIST:
                out.add("list");
                break;
            case ADD:
                out.add("add");
                if (addName != null && OpenClawStrings.isNotBlank(addName)) {
                    out.add(addName.trim());
                }
                break;
            case BINDINGS:
                out.add("bindings");
                break;
            case BIND:
                out.add("bind");
                break;
            case UNBIND:
                out.add("unbind");
                break;
            case SET_IDENTITY:
                out.add("set-identity");
                break;
            case DELETE:
                out.add("delete");
                if (deleteAgentId != null && OpenClawStrings.isNotBlank(deleteAgentId)) {
                    out.add(deleteAgentId.trim());
                }
                break;
            default:
                break;
        }
        if (verb == Verb.DEFAULT_LIST || verb == Verb.LIST) {
            OpenClawCliArgv.addFlag(out, "--json", listJson);
            OpenClawCliArgv.addFlag(out, "--bindings", listBindings);
        }
        if (verb == Verb.ADD) {
            OpenClawCliArgv.addIfPresent(out, "--workspace", workspace);
            OpenClawCliArgv.addIfPresent(out, "--model", model);
            OpenClawCliArgv.addIfPresent(out, "--agent-dir", agentDir);
            OpenClawCliArgv.addRepeatable(out, "--bind", bindValues);
            OpenClawCliArgv.addFlag(out, "--non-interactive", nonInteractive);
            OpenClawCliArgv.addFlag(out, "--json", addJson);
        }
        if (verb == Verb.BINDINGS) {
            OpenClawCliArgv.addIfPresent(out, "--agent", bindingsAgent);
            OpenClawCliArgv.addFlag(out, "--json", bindingsJson);
        }
        if (verb == Verb.BIND) {
            OpenClawCliArgv.addIfPresent(out, "--agent", bindAgent);
            OpenClawCliArgv.addRepeatable(out, "--bind", bindValues);
            OpenClawCliArgv.addFlag(out, "--json", bindJson);
        }
        if (verb == Verb.UNBIND) {
            OpenClawCliArgv.addIfPresent(out, "--agent", unbindAgent);
            OpenClawCliArgv.addRepeatable(out, "--bind", bindValues);
            OpenClawCliArgv.addFlag(out, "--all", unbindAll);
            OpenClawCliArgv.addFlag(out, "--json", unbindJson);
        }
        if (verb == Verb.DELETE) {
            OpenClawCliArgv.addFlag(out, "--force", deleteForce);
            OpenClawCliArgv.addFlag(out, "--json", deleteJson);
        }
        if (verb == Verb.SET_IDENTITY) {
            OpenClawCliArgv.addIfPresent(out, "--agent", identityAgent);
            OpenClawCliArgv.addIfPresent(out, "--workspace", identityWorkspace);
            OpenClawCliArgv.addIfPresent(out, "--identity-file", identityFile);
            OpenClawCliArgv.addFlag(out, "--from-identity", fromIdentity);
            OpenClawCliArgv.addIfPresent(out, "--name", identityName);
            OpenClawCliArgv.addIfPresent(out, "--theme", identityTheme);
            OpenClawCliArgv.addIfPresent(out, "--emoji", identityEmoji);
            OpenClawCliArgv.addIfPresent(out, "--avatar", identityAvatar);
            OpenClawCliArgv.addFlag(out, "--json", identityJson);
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * 构建 {@link AgentsOptions}。
     */
    public static final class Builder {
        private Verb verb = Verb.DEFAULT_LIST;
        private boolean listJson;
        private boolean listBindings;
        private String addName;
        private String workspace;
        private String model;
        private String agentDir;
        private List<String> bindValues = new ArrayList<>();
        private boolean nonInteractive;
        private boolean addJson;
        private String bindingsAgent;
        private boolean bindingsJson;
        private String bindAgent;
        private boolean bindJson;
        private String unbindAgent;
        private boolean unbindAll;
        private boolean unbindJson;
        private String deleteAgentId;
        private boolean deleteForce;
        private boolean deleteJson;
        private String identityAgent;
        private String identityWorkspace;
        private String identityFile;
        private boolean fromIdentity;
        private String identityName;
        private String identityTheme;
        private String identityEmoji;
        private String identityAvatar;
        private boolean identityJson;
        private List<String> extra = new ArrayList<>();

        /** 隐式 list（与裸 {@code openclaw agents} 一致）。 */
        public Builder defaultList() {
            this.verb = Verb.DEFAULT_LIST;
            return this;
        }

        /** 显式 {@code agents list}。 */
        public Builder list() {
            this.verb = Verb.LIST;
            return this;
        }

        /** list / 默认 list：{@code --json}。 */
        public Builder listJson(boolean json) {
            this.listJson = json;
            return this;
        }

        /** list：{@code --bindings}。 */
        public Builder listBindings(boolean bindings) {
            this.listBindings = bindings;
            return this;
        }

        /** {@code agents add [name]}。 */
        public Builder add(String name) {
            this.verb = Verb.ADD;
            this.addName = name;
            return this;
        }

        public Builder workspace(String workspace) {
            this.workspace = workspace;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder agentDir(String agentDir) {
            this.agentDir = agentDir;
            return this;
        }

        /** 可重复的 {@code --bind}。 */
        public Builder bind(String channelBinding) {
            if (channelBinding != null && OpenClawStrings.isNotBlank(channelBinding)) {
                this.bindValues.add(channelBinding.trim());
            }
            return this;
        }

        public Builder nonInteractive(boolean nonInteractive) {
            this.nonInteractive = nonInteractive;
            return this;
        }

        public Builder addJson(boolean json) {
            this.addJson = json;
            return this;
        }

        public Builder bindings() {
            this.verb = Verb.BINDINGS;
            return this;
        }

        public Builder bindingsAgent(String agent) {
            this.bindingsAgent = agent;
            return this;
        }

        public Builder bindingsJson(boolean json) {
            this.bindingsJson = json;
            return this;
        }

        public Builder bindCommand() {
            this.verb = Verb.BIND;
            this.bindValues = new ArrayList<>();
            return this;
        }

        public Builder bindAgent(String agent) {
            this.bindAgent = agent;
            return this;
        }

        public Builder bindJson(boolean json) {
            this.bindJson = json;
            return this;
        }

        public Builder unbind() {
            this.verb = Verb.UNBIND;
            this.bindValues = new ArrayList<>();
            return this;
        }

        public Builder unbindAgent(String agent) {
            this.unbindAgent = agent;
            return this;
        }

        public Builder unbindAll(boolean all) {
            this.unbindAll = all;
            return this;
        }

        public Builder unbindJson(boolean json) {
            this.unbindJson = json;
            return this;
        }

        /** {@code agents delete <id>}。 */
        public Builder delete(String agentId) {
            this.verb = Verb.DELETE;
            this.deleteAgentId = agentId;
            return this;
        }

        public Builder deleteForce(boolean force) {
            this.deleteForce = force;
            return this;
        }

        public Builder deleteJson(boolean json) {
            this.deleteJson = json;
            return this;
        }

        public Builder setIdentity() {
            this.verb = Verb.SET_IDENTITY;
            return this;
        }

        public Builder identityAgent(String agent) {
            this.identityAgent = agent;
            return this;
        }

        public Builder identityWorkspace(String workspace) {
            this.identityWorkspace = workspace;
            return this;
        }

        public Builder identityFile(String path) {
            this.identityFile = path;
            return this;
        }

        public Builder fromIdentity(boolean fromIdentity) {
            this.fromIdentity = fromIdentity;
            return this;
        }

        public Builder identityName(String name) {
            this.identityName = name;
            return this;
        }

        public Builder identityTheme(String theme) {
            this.identityTheme = theme;
            return this;
        }

        public Builder identityEmoji(String emoji) {
            this.identityEmoji = emoji;
            return this;
        }

        public Builder identityAvatar(String avatar) {
            this.identityAvatar = avatar;
            return this;
        }

        public Builder identityJson(boolean json) {
            this.identityJson = json;
            return this;
        }

        /**
         * 追加原始 token（文档新增或未建模 flag）。
         *
         * @param tokens argv 片段
         * @return this
         */
        public Builder extra(String... tokens) {
            if (tokens != null) {
                Collections.addAll(extra, tokens);
            }
            return this;
        }

        public AgentsOptions build() {
            return new AgentsOptions(this);
        }
    }
}
