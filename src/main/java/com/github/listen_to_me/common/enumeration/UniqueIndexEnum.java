package com.github.listen_to_me.common.enumeration;

/**
 * 唯一索引枚举
 * 在全局异常处理器中使用，对 DuplicateKeyException 异常里的重复索引名进行更友好的说明
 */
public enum UniqueIndexEnum {
    UK_PHONE("phone", "手机号"),
    UK_EMAIL("email", "邮箱"),
    UK_USERNAME("username", "用户名");

    private final String indexName;
    private final String fieldDesc;

    UniqueIndexEnum(String indexName, String fieldDesc) {
        this.indexName = indexName;
        this.fieldDesc = fieldDesc;
    }

    public static String getFieldDescByIndex(String indexName) {
        for (UniqueIndexEnum indexEnum : values()) {
            if (indexEnum.indexName.equals(indexName)) {
                return indexEnum.fieldDesc;
            }
        }
        return indexName;
    }
}
