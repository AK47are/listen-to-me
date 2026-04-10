package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.HistoryProgressDTO;
import com.github.listen_to_me.domain.entity.PlayHistory;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.AudioVO;

public interface IPlayHistoryService extends IService<PlayHistory> {

    void addPlayHistory(HistoryProgressDTO historyProgressDTO);

    IPage<AudioVO> getHistoryPage(PageQuery pageQuery);

    Integer findPlayHistory(Long audioId);
}
