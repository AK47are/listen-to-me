package com.github.listen_to_me.service.impl;

import com.github.listen_to_me.domain.entity.OrderInfo;
import com.github.listen_to_me.mapper.OrderInfoMapper;
import com.github.listen_to_me.service.IOrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements IOrderInfoService {

}
