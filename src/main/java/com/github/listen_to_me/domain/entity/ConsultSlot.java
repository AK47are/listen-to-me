package com.github.listen_to_me.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
@Getter
@Setter
@TableName("consult_slot")
public class ConsultSlot implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long creatorId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 0-可选, 1-锁定(下单中), 2-已约, 3-完成
     */
    private Integer status;
}
