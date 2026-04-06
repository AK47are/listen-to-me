package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.entity.AudioOrder;
import com.github.listen_to_me.domain.vo.AudioOrderDetailVO;
import com.github.listen_to_me.domain.vo.AudioOrderVO;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
public interface IAudioOrderService extends IService<AudioOrder> {

    AudioOrderVO purchaseAudio(Long audioId);

    AudioOrderDetailVO queryAudioOrderDetail(String sn);
}
