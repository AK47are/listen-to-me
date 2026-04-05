package com.github.listen_to_me.common.enumeration;

import lombok.Getter;
import java.util.concurrent.TimeUnit;

@Getter
public enum RedisKey {
    USER_PERMS("user:perms:", 12L, TimeUnit.HOURS),
    USER_HISTORY("user:history:", 30L, TimeUnit.MINUTES),
    IMAGE_CAPTCHA("captcha:codes:", 2L, TimeUnit.MINUTES),
    VERIFY_CODE("captcha:verify:", 5L, TimeUnit.MINUTES),
    TEMP_AUDIO_URL("temp:audio:", 10L, TimeUnit.MINUTES),
    TEMP_COVER_URL("temp:cover:", 10L, TimeUnit.MINUTES),
    HOT_AUDIO_RANK("hot:audio:rank", 30L, TimeUnit.DAYS),
    ONLINE_RAW_URL("online:audio:raw:", 30L, TimeUnit.MINUTES),
    ONLINE_CLIP_URL("online:audio:clip:", 7L, TimeUnit.MINUTES),
    ONLINE_IMG_URL("online:img:", 18L, TimeUnit.HOURS),
    USER_PLAY_COUNTED("user:play:counted", 1L, TimeUnit.HOURS);
    private final String prefix;
    private final Long expire;
    private final TimeUnit unit;

    // 资源前缀、有效期和时间单位
    RedisKey(String prefix, Long expire, TimeUnit unit) {
        this.prefix = prefix;
        this.expire = expire;
        this.unit = unit;
    }

    public String join(String suffix) {
        return this.prefix + suffix;
    }
}
