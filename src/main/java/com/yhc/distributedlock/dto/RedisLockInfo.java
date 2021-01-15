package com.yhc.distributedlock.dto;

public class RedisLockInfo {

    // redislock key
    private String key;
    // redislock 有效期
    private long expireTime;
    // redislock 总续期次数
    private int tryCount;
    // redislock 已续期次数
    private int tryNumber;
    // 当前请求线程
    private Thread thread;

    public RedisLockInfo(String key, long expireTime, int tryCount, Thread thread) {
        this.key = key;
        this.expireTime = expireTime;
        this.tryCount = tryCount;
        this.thread = thread;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public int getTryCount() {
        return tryCount;
    }

    public void setTryCount(int tryCount) {
        this.tryCount = tryCount;
    }

    public int getTryNumber() {
        return tryNumber;
    }

    public void setTryNumber(int tryNumber) {
        this.tryNumber = tryNumber;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
}
