package com.yhc.distributedlock.contants;

public class RedisLockConts {

    /** redis锁，跑批周期时间，单位：S */
    public static final int JOB_PERIOD = 5;

    /** redis锁，剩余有效期临界点，若小于此值，则重新续期 */
    public static final long KEY_TTL = 10;
}
