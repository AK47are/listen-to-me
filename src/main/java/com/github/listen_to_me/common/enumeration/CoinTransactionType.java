package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum CoinTransactionType implements DescribedEnum<String> {
    INCOME("INCOME", "收入"),
    EXPENSE("EXPENSE", "支出");

    public static final String SCHEMA_DESC = "交易类型: INCOME(收入), EXPENSE(支出)";

    @EnumValue
    private final String code;
    private final String label;

    CoinTransactionType(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
