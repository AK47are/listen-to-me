package com.github.listen_to_me.service.impl;

import com.github.listen_to_me.domain.entity.PlayHistory;
import com.github.listen_to_me.mapper.PlayHistoryMapper;
import com.github.listen_to_me.service.IPlayHistoryService;
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
public class PlayHistoryServiceImpl extends ServiceImpl<PlayHistoryMapper, PlayHistory> implements IPlayHistoryService {

}
