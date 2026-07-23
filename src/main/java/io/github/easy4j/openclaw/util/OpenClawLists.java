package io.github.easy4j.openclaw.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 列表工具（Java 8 兼容），用于替代 {@link List#of()}、{@link List#copyOf(Collection)} 等 JDK 9+ API。
 */
public final class OpenClawLists {

    private OpenClawLists() {
    }

    /**
     * 返回不可变空列表（等价于 {@code List.of()}）。
     */
    public static <T> List<T> empty() {
        return Collections.emptyList();
    }

    /**
     * 由可变参数构造不可变列表（等价于 {@code List.of(e1, e2, ...)}）。
     */
    @SafeVarargs
    public static <T> List<T> of(T... elements) {
        if (elements == null || elements.length == 0) {
            return empty();
        }
        return Collections.unmodifiableList(Arrays.asList(elements.clone()));
    }

    /**
     * 复制为不可变列表（等价于 {@code List.copyOf(source)}）。
     */
    public static <T> List<T> copyOf(Collection<? extends T> source) {
        if (source == null || source.isEmpty()) {
            return empty();
        }
        return Collections.unmodifiableList(new ArrayList<>(source));
    }
}
