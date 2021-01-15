package com.yhc.distributedlock.aop;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.yhc.distributedlock.annotation.RedisLock;
import com.yhc.distributedlock.contants.RedisLockConts;
import com.yhc.distributedlock.dto.RedisLockInfo;
import com.yhc.distributedlock.exception.DlException;
import com.yhc.distributedlock.plugin.RedisService;

@Aspect
@Configuration
public class RedisLockHandler {

	private static final Logger log = LoggerFactory.getLogger(RedisLockHandler.class);

	private RedisService redisService;

	@Autowired
	public void setRedisService(RedisService redisService) {
		this.redisService = redisService;
	}

	@Pointcut("@annotation(com.yhc.distributedlock.annotation.RedisLock)")
	public void rlPointCut() {
	}

	// 考虑过用volatile关键字来实现线程中断，但是每个请求线程之前的变量应该是独享的，所以我这里还是考虑传递线程对象
//	    private volatile boolean threadInternalFlag = true;

	@Around("rlPointCut()")
	public Object redisLock(ProceedingJoinPoint pjp) throws Throwable {
		Object result;

		// 获取注解对象
		RedisLock redisLockAnnotation = getDeclaredAnnotation(pjp);
		String lockKey = redisLockAnnotation.key();
		log.info("# [BEGIN]分布式锁,创建key:{}", lockKey);
		try {
			boolean dlFlag = redisService.setnx(lockKey, UUID.randomUUID().toString(), redisLockAnnotation.expire());
			if (!dlFlag) {
				throw new DlException("# 业务繁忙，请稍后重试");
			}
			// 添加续期任务到JOB中
			lockKeyList.add(new RedisLockInfo(lockKey, redisLockAnnotation.expire(), redisLockAnnotation.tryCount(),
					Thread.currentThread()));
			result = pjp.proceed();
			redisService.delete(lockKey);
			log.info("# [END]分布式锁,删除key:{}", lockKey);
			return result;
		} catch (DlException de) {
			log.warn("# 分布式锁，{}", de.getMsg());
		} catch (InterruptedException ie) {
			log.error("# 分布式锁,请求超时", ie.getMessage());
			throw new Exception(ie.getMessage());
		} catch (Exception e) {
			log.error("# 分布式锁,创建失败", e);
			redisService.delete(lockKey);
			log.info("# [END]分布式锁,删除key:{}", lockKey);
		}
		// 看到很多开发在这里清理key，但是你想想，
		// 上个请求仍在继续，如果第二个请求进来，setnx肯定失败，会进入finally删除key，第三个请求是不是可以正常请求了呢
		// finally { }
		return false;
	}

	// 获取注解对象
	public RedisLock getDeclaredAnnotation(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
		// 获取方法名
		String methodName = joinPoint.getSignature().getName();
		// 反射获取目标类
		Class<?> targetClass = joinPoint.getTarget().getClass();
		// 拿到方法对应的参数类型
		Class<?>[] parameterTypes = ((MethodSignature) joinPoint.getSignature()).getParameterTypes();
		// 根据类、方法、参数类型（重载）获取到方法的具体信息
		Method objMethod = targetClass.getMethod(methodName, parameterTypes);
		// 拿到方法定义的注解信息
		RedisLock annotation = objMethod.getDeclaredAnnotation(RedisLock.class);
		// 返回
		return annotation;
	}

	// 防止分布式锁设置时间过短而过期，但请求所需时间较长线程仍在运行种，这样会造成分布式锁失效，所以增加锁续期机制
//	private static List<RedisLockInfo> lockKeyList = Collections.synchronizedList(new ArrayList<RedisLockInfo>());
	private static Queue<RedisLockInfo> lockKeyList = new ConcurrentLinkedQueue<RedisLockInfo>();

	@PostConstruct
	public void redisLockRenewJob() {
		log.info("# redis lock renew job started");

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					jobRun();
				} catch (Exception e) {
					log.error("# job error", e);
				}
			}
		}), 0, RedisLockConts.JOB_PERIOD, TimeUnit.SECONDS);
	}

	private void jobRun() {
		Iterator<RedisLockInfo> it = lockKeyList.iterator();
		while (it.hasNext()) {
			RedisLockInfo redisLockInfo = it.next();
			String redisLockKey = redisLockInfo.getKey();

			// 这里为何不是判断key是否存在，是为了避免这里判断存在之后和续期的中间时间，新的请求进来重新setnx，造成两个请求同时存在
			long ttl = redisService.getExpire(redisLockKey);
			if (ttl < RedisLockConts.KEY_TTL && ttl > 0) {
				int tryNumber = redisLockInfo.getTryNumber();
				int tryCount = redisLockInfo.getTryCount();
				if (tryNumber >= tryCount) {
					log.error("# thread interrupt");
					// 续期次数已用完，直接中断线程
					it.remove();
					Thread thread = redisLockInfo.getThread();
					thread.interrupt();
					continue;
				}

				boolean result = redisService.expire(redisLockKey, redisLockInfo.getExpireTime());
				if (result) {
					tryNumber += 1;
					redisLockInfo.setTryNumber(tryNumber);
					log.info("# redis锁[key:{}],检查临近过期,完成进行第{}次续期，{}次续期后将终止请求", redisLockKey, tryNumber, tryCount);
				} else {
					// 这里可能请求线程已经完成，所以续期失败。
					it.remove();
					log.info("# redis锁-EXPIRE[key:{}],请求已完成，无须续期", redisLockKey);
				}

			} else if (ttl == -2) { // -2表示key不存在
				// 这里可能请求线程已经完成，所以续期失败。
				it.remove();
				log.info("# redis锁-TTL[key:{}],请求已完成，无须续期", redisLockKey);
			}
		}
	}

}
