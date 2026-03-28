package com.github.listen_to_me.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExceptionUtils {
    // 正则匹配 MySQL 唯一约束冲突的索引名
    private static final Pattern INDEX_PATTERN = Pattern.compile("for key '([^']+)'");

    // 从 DuplicateKeyException 中解析出索引名
    public static String getDuplicateIndexName(Throwable throwable) {
        String message = throwable.getMessage();
        Matcher matcher = INDEX_PATTERN.matcher(message);
        if (matcher.find()) {
            String index = matcher.group(1);
            return index.contains(".") ? index.split("\\.")[1] : index;
        }
        return null;
    }
}
