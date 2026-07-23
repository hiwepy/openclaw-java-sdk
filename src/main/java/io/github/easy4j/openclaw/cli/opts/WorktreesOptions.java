package io.github.easy4j.openclaw.cli.opts;

import io.github.easy4j.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw worktrees}：创建、检查、恢复与清理受管 worktree。
 * <p>
 * 支持 {@code list}、{@code create <repoRoot>}、{@code remove <id>}、{@code restore <id>}、{@code gc} 子命令。
 * 父命令默认执行帮助。
 * </p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/worktrees">worktrees CLI</a>
 */
public final class WorktreesOptions implements CliSubArgs {

    /** 子命令模式。 */
    public enum Mode {
        /** {@code list}：列出受管 worktree。 */
        LIST,
        /** {@code create <repoRoot>}：创建受管 worktree。 */
        CREATE,
        /** {@code remove <id>}：移除受管 worktree。 */
        REMOVE,
        /** {@code restore <id>}：恢复受管 worktree。 */
        RESTORE,
        /** {@code gc}：垃圾回收。 */
        GC
    }

    private final Mode mode;
    /** create：位置参数 {@code <repoRoot>} 源 git checkout。 */
    private final String repoRoot;
    /** remove/restore：位置参数 {@code <id>} 受管 worktree id。 */
    private final String id;
    /** create：{@code --name} 受管 worktree 名称。 */
    private final String name;
    /** create：{@code --base-ref} 分支来源 Git ref。 */
    private final String baseRef;
    /** remove：{@code --force} 即使快照创建失败也移除。 */
    private final boolean force;
    /** {@code --json}：JSON 输出。 */
    private final boolean json;

    private WorktreesOptions(Builder b) {
        this.mode = b.mode;
        this.repoRoot = b.repoRoot;
        this.id = b.id;
        this.name = b.name;
        this.baseRef = b.baseRef;
        this.force = b.force;
        this.json = b.json;
    }

    /**
     * @return 新 {@link Builder}（默认 {@link Mode#LIST}）
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<String> toSubcommandArguments() {
        List<String> out = new ArrayList<>();
        switch (mode) {
            case LIST:
                out.add("list");
                break;
            case CREATE:
                out.add("create");
                if (repoRoot != null && !repoRoot.isEmpty()) {
                    out.add(repoRoot);
                }
                break;
            case REMOVE:
                out.add("remove");
                if (id != null && !id.isEmpty()) {
                    out.add(id);
                }
                break;
            case RESTORE:
                out.add("restore");
                if (id != null && !id.isEmpty()) {
                    out.add(id);
                }
                break;
            case GC:
                out.add("gc");
                break;
            default:
                break;
        }
        OpenClawCliArgv.addIfPresent(out, "--name", name);
        OpenClawCliArgv.addIfPresent(out, "--base-ref", baseRef);
        OpenClawCliArgv.addFlag(out, "--force", force);
        OpenClawCliArgv.addFlag(out, "--json", json);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link WorktreesOptions} 构建器。
     */
    public static final class Builder {
        private Mode mode = Mode.LIST;
        private String repoRoot;
        private String id;
        private String name;
        private String baseRef;
        private boolean force;
        private boolean json;

        /** 切换为 {@code list} 子命令。 */
        public Builder list() { this.mode = Mode.LIST; return this; }
        /** 切换为 {@code create <repoRoot>} 子命令。 */
        public Builder create(String repoRoot) { this.mode = Mode.CREATE; this.repoRoot = repoRoot; return this; }
        /** 切换为 {@code remove <id>} 子命令。 */
        public Builder remove(String id) { this.mode = Mode.REMOVE; this.id = id; return this; }
        /** 切换为 {@code restore <id>} 子命令。 */
        public Builder restore(String id) { this.mode = Mode.RESTORE; this.id = id; return this; }
        /** 切换为 {@code gc} 子命令。 */
        public Builder gc() { this.mode = Mode.GC; return this; }
        /** 显式指定 {@link Mode}。 */
        public Builder mode(Mode mode) { this.mode = mode; return this; }
        /** create：位置参数 {@code <repoRoot>} 源 git checkout。 */
        public Builder repoRoot(String repoRoot) { this.repoRoot = repoRoot; return this; }
        /** remove/restore：位置参数 {@code <id>} 受管 worktree id。 */
        public Builder id(String id) { this.id = id; return this; }
        /** create：{@code --name} 受管 worktree 名称。 */
        public Builder name(String name) { this.name = name; return this; }
        /** create：{@code --base-ref} 分支来源 Git ref。 */
        public Builder baseRef(String baseRef) { this.baseRef = baseRef; return this; }
        /** remove：{@code --force} 即使快照创建失败也移除。 */
        public Builder force(boolean force) { this.force = force; return this; }
        /** {@code --json}：JSON 输出。 */
        public Builder json(boolean json) { this.json = json; return this; }

        /**
         * @return 不可变 {@link WorktreesOptions}
         */
        public WorktreesOptions build() {
            return new WorktreesOptions(this);
        }
    }
}
