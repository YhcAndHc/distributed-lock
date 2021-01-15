package com.yhc.distributedlock.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLock {

    /**
     * 【必须】redis锁 key的前缀
     *
     * @return
     */
    String key() default "";

    /**
     * redis锁 过期时间，默认60秒，最小不得低于10秒
     *
     * @return
     */
    long expire() default 60;

    /**
     * redis锁 续期重试次数
     * 每五秒扫描一次，若发现锁剩余有效期低于10秒，且当前线程未执行完，则自动续期，续期时间为expire参数
     *
     * @return
     */
    int tryCount() default 3;
}
