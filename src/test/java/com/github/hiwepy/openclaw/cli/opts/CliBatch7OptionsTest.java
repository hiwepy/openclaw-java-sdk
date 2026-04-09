package com.github.hiwepy.openclaw.cli.opts;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * B7：backup / update / uninstall / reset 强类型 argv 映射烟测。
 */
class CliBatch7OptionsTest {

    @Test
    void backup_create_and_verify() {
        assertEquals(
                List.of("create", "--output", "~/Backups", "--dry-run", "--json"),
                BackupOptions.builder().create().output("~/Backups").dryRun(true).json(true).build().toSubcommandArguments());
        assertEquals(
                List.of("verify", "./backup.tar.gz"),
                BackupOptions.builder().verify("./backup.tar.gz").build().toSubcommandArguments());
    }

    @Test
    void update_status() {
        assertEquals(
                List.of("status", "--json", "--timeout", "10"),
                UpdateOptions.builder().status().json(true).timeout("10").build().toSubcommandArguments());
        assertEquals(
                List.of("--channel", "beta", "--yes"),
                UpdateOptions.builder().update().channel("beta").yes(true).build().toSubcommandArguments());
    }

    @Test
    void uninstall_and_reset() {
        assertEquals(
                List.of("--all", "--yes"),
                UninstallOptions.builder().all(true).yes(true).build().toSubcommandArguments());
        assertEquals(
                List.of("--scope", "config", "--yes", "--non-interactive"),
                ResetOptions.builder()
                        .scope(ResetOptions.Scope.CONFIG)
                        .yes(true)
                        .nonInteractive(true)
                        .build()
                        .toSubcommandArguments());
    }
}
