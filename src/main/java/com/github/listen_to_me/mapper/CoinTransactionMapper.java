package com.github.listen_to_me.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.listen_to_me.domain.entity.CoinTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CoinTransactionMapper extends BaseMapper<CoinTransaction> {
}
