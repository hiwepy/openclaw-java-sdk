package io.github.easy4j.openclaw.cli.support;

import io.github.easy4j.openclaw.OpenClawCliConfig;
import io.github.easy4j.openclaw.cli.OpenClawCliExecutor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

/**
 * 安装可执行的 mock {@code openclaw} 脚本，用于 CLI 可用性探测单测。
 *
 * @author wandl
 * @since 1.0.0
 */
public final class MockOpenClawCli {

    private final Path scriptPath;

    private MockOpenClawCli(Path scriptPath) {
        this.scriptPath = scriptPath;
    }

    /**
     * 在临时目录安装 mock CLI。
     */
    public static MockOpenClawCli install() throws IOException {
        Path root = Files.createTempDirectory("openclaw-mock-cli-");
        Path script = root.resolve("openclaw");
        Files.write(script, buildScript().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        makeExecutable(script);
        return new MockOpenClawCli(script);
    }

    /**
     * 构造绑定 mock 可执行文件的配置。
     */
    public OpenClawCliConfig newConfig() {
        OpenClawCliConfig config = new OpenClawCliConfig();
        config.setExecutable(scriptPath.toAbsolutePath().toString());
        config.setProbeTimeoutSeconds(5);
        config.setTimeout(5);
        return config;
    }

    /**
     * 构造绑定 mock 的执行器。
     */
    public OpenClawCliExecutor newExecutor() {
        return new OpenClawCliExecutor(newConfig());
    }

    public Path scriptPath() {
        return scriptPath;
    }

    private static String buildScript() {
        return "#!/usr/bin/env bash\n"
                + "set -euo pipefail\n"
                + "for arg in \"$@\"; do\n"
                + "if [ \"$arg\" = \"--version\" ] || [ \"$arg\" = \"-V\" ]; then\n"
                + "echo 'openclaw mock 1.0.0'\n"
                + "exit 0\n"
                + "fi\n"
                + "done\n"
                + "echo \"unsupported argv: $*\" >&2\n"
                + "exit 1\n";
    }

    private static void makeExecutable(Path script) throws IOException {
        try {
            Set<PosixFilePermission> perms = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.GROUP_READ,
                    PosixFilePermission.GROUP_EXECUTE,
                    PosixFilePermission.OTHERS_READ,
                    PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(script, perms);
        } catch (UnsupportedOperationException ex) {
            script.toFile().setExecutable(true);
        }
    }
}
