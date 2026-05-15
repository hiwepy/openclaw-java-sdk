package io.github.hiwepy.openclaw.cli.opts;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * B6：models / security / secrets / sandbox 强类型 argv 映射烟测。
 */
class CliBatch6OptionsTest {

    @Test
    void models_status_and_auth_login() {
        assertEquals(
                List.of("status", "--json", "--probe", "--agent", "ops"),
                ModelsOptions.builder().status().statusJson(true).probe(true).agent("ops").build().toSubcommandArguments());
        assertEquals(
                List.of("auth", "login", "--provider", "openai-codex", "--set-default"),
                ModelsOptions.builder()
                        .authLogin("openai-codex")
                        .authSetDefault(true)
                        .build()
                        .toSubcommandArguments());
    }

    @Test
    void security_audit() {
        assertEquals(
                List.of("audit", "--deep", "--json"),
                SecurityOptions.builder().audit().deep(true).json(true).build().toSubcommandArguments());
    }

    @Test
    void secrets_reload_and_apply() {
        assertEquals(
                List.of("reload", "--url", "ws://127.0.0.1:18789", "--json"),
                SecretsOptions.builder().reload().gatewayUrl("ws://127.0.0.1:18789").json(true).build().toSubcommandArguments());
        assertEquals(
                List.of("apply", "--from", "/tmp/plan.json", "--dry-run"),
                SecretsOptions.builder().apply("/tmp/plan.json").dryRun(true).build().toSubcommandArguments());
    }

    @Test
    void sandbox_recreate() {
        assertEquals(
                List.of("recreate", "--all", "--browser", "--force"),
                SandboxOptions.builder().recreate().recreateAll(true).recreateBrowser(true).force(true).build().toSubcommandArguments());
    }
}
