package io.github.hiwepy.openclaw.cli.opts;

import io.github.hiwepy.openclaw.util.OpenClawLists;
import io.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw directory}：在支持目录能力的渠道上查询联系人、群组与「自己」信息，供 {@code openclaw message send --target} 等命令粘贴 ID。
 * <p>多渠道配置时通常必须指定 {@code --channel}；单渠道时可省略。默认人机输出为制表符分隔，脚本建议 {@code --json}。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/directory">directory CLI</a>
 */
public final class DirectoryOptions implements CliSubArgs {

    /**
     * 目录子命令模式，决定生成的 token 序列（{@code self}、{@code peers list} 等）。
     */
    public enum Mode {
        /** {@code directory self} */
        SELF,
        /** {@code directory peers list} */
        PEERS_LIST,
        /** {@code directory groups list} */
        GROUPS_LIST,
        /** {@code directory groups members}（需 {@link Builder#groupId}） */
        GROUPS_MEMBERS
    }

    /**
     * 目录子模式：{@code self}、{@code peers list}、{@code groups list}、{@code groups members}（与文档示例一致）。
     */
    private final Mode mode;
    /**
     * {@code --channel}：渠道 id 或别名（多渠道时必需；仅配一个渠道时可自动推断）。
     */
    private final String channel;
    /**
     * {@code --account}：账号 id（文档默认取渠道默认账号）。
     */
    private final String account;
    /**
     * {@code --json}：JSON 输出，便于脚本解析。
     */
    private final boolean json;
    /**
     * {@code --query}：过滤 peers/groups 列表的查询子串（文档示例为名称片段）。
     */
    private final String query;
    /**
     * {@code --limit}：列表返回条数上限（文档 peers 示例为 50）。
     */
    private final Integer limit;
    /**
     * {@code --group-id}：{@code groups members} 子命令所需的群组标识。
     */
    private final String groupId;
    /**
     * 其它未建模参数。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private DirectoryOptions(Builder b) {
        this.mode = b.mode;
        this.channel = b.channel;
        this.account = b.account;
        this.json = b.json;
        this.query = b.query;
        this.limit = b.limit;
        this.groupId = b.groupId;
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
        switch (mode) {
            case SELF:
                out.add("self");
                break;
            case PEERS_LIST:
                out.add("peers");
                out.add("list");
                break;
            case GROUPS_LIST:
                out.add("groups");
                out.add("list");
                break;
            case GROUPS_MEMBERS:
                out.add("groups");
                out.add("members");
                break;
            default:
                break;
        }
        OpenClawCliArgv.addIfPresent(out, "--channel", channel);
        OpenClawCliArgv.addIfPresent(out, "--account", account);
        OpenClawCliArgv.addIfPresent(out, "--query", query);
        OpenClawCliArgv.addIfNotNull(out, "--limit", limit);
        OpenClawCliArgv.addIfPresent(out, "--group-id", groupId);
        OpenClawCliArgv.addFlag(out, "--json", json);
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link DirectoryOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.PEERS_LIST;
        private String channel;
        private String account;
        private boolean json;
        private String query;
        private Integer limit;
        private String groupId;
        private List<String> extra = new ArrayList<>();

        /**
         * @return {@code this}，模式设为 {@link Mode#SELF}
         */
        public Builder self() {
            this.mode = Mode.SELF;
            return this;
        }

        /**
         * @return {@code this}，模式设为 {@link Mode#PEERS_LIST}
         */
        public Builder peersList() {
            this.mode = Mode.PEERS_LIST;
            return this;
        }

        /**
         * @return {@code this}，模式设为 {@link Mode#GROUPS_LIST}
         */
        public Builder groupsList() {
            this.mode = Mode.GROUPS_LIST;
            return this;
        }

        /**
         * @param groupId 群组 ID（{@code groups members}）
         * @return {@code this}
         */
        public Builder groupsMembers(String groupId) {
            this.mode = Mode.GROUPS_MEMBERS;
            this.groupId = groupId;
            return this;
        }

        /**
         * @param channel {@code --channel}
         * @return {@code this}
         */
        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        /**
         * @param account {@code --account}
         * @return {@code this}
         */
        public Builder account(String account) {
            this.account = account;
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
         * @param query {@code --query}
         * @return {@code this}
         */
        public Builder query(String query) {
            this.query = query;
            return this;
        }

        /**
         * @param limit {@code --limit}
         * @return {@code this}
         */
        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        /**
         * @param tokens 额外 CLI token
         * @return {@code this}
         */
        public Builder extra(String... tokens) {
            if (tokens != null) {
                Collections.addAll(extra, tokens);
            }
            return this;
        }

        /**
         * @return 不可变 {@link DirectoryOptions}
         */
        public DirectoryOptions build() {
            return new DirectoryOptions(this);
        }
    }
}
