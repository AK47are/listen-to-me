package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.domain.entity.CoinTransaction;
import com.github.listen_to_me.mapper.CoinTransactionMapper;
import com.github.listen_to_me.service.ICoinTransactionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CoinTransactionServiceImpl extends ServiceImpl<CoinTransactionMapper, CoinTransaction>
        implements ICoinTransactionService {
}
