package com.ctg.test.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * spring redis 工具类
 * created by liang on 2021/03/18 .
 * @author qsdi
 */
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Component
public class RedisService {

    @Autowired
    @Qualifier(value = "ivdgRedisTemplate")
    public RedisTemplate redisTemplate;

    /**
     * 获得锁有效时间
     */
    @Value("${spring.redis.lock.timeout:3000}")
    private long lockTimeout;

//    @Value("${lock.sleep.interval.time:30}")
//    private long sleepIntervalTime;

    /**
     * 判断key是否存在
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Long timeout, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 设置过期时间（指定到期时间）
     *
     * @param key
     * @param date
     * @return
     */
    public Boolean expireAt(String key, Date date) {
        return redisTemplate.expireAt(key, date);
    }


    /**
     * 通过increment(K key, long delta)方法以增量方式存储long值（正值则自增，负值则自减）
     *
     * @param key
     * @param increment
     * @return
     */
    public Long incrBy(String key, long increment) {
        return redisTemplate.opsForValue().increment(key, increment);
    }

    /**
     * 如果旧值存在时，将旧值改为新值
     * @param oldKey
     * @param newKey
     * @return
     */
    public boolean renameIfAbsent(String oldKey,String newKey){
        return redisTemplate.renameIfAbsent(oldKey,newKey);
    }


    /**
     * 将旧值改为新值
     * @param oldKey
     * @param newKey
     */
    public void rename(String oldKey,String newKey){
        redisTemplate.rename(oldKey,newKey);
    }


    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        try {
            SessionCallback sessionCallback = new SessionCallback() {
                @Override
                public Object execute(RedisOperations redisOperations) throws DataAccessException {
                    redisOperations.multi();
                    redisOperations.opsForValue().setIfAbsent(key, null, timeout, unit);
                    redisOperations.expire(key, timeout, unit);
                    return redisOperations.exec();
                }
            };
            redisTemplate.execute(sessionCallback);
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public boolean deleteObject(final String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return
     */
    public long deleteObject(final Collection collection) {
        return redisTemplate.delete(collection);
    }

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存Set
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet) {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext()) {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(final String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 尝试获取锁，立即返回结果
     *
     * @param key 锁key
     * @return boolean
     */
    public boolean tryLock(String key) {
        if (StringUtils.isEmpty(key)) {
            return true;
        }
        return lock(key, lockTimeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 尝试获取锁，立即返回结果，指定锁过期时间
     *
     * @param key  key
     * @param time 锁过期时间
     * @param unit 时间单位
     * @return boolean
     */
    public boolean lock(String key, long time, TimeUnit unit) {
        long lockTime = unit.toMillis(time);
        final ValueOperations<String, String> ops = redisTemplate.opsForValue();
        if (ops.setIfAbsent(key, getCacheValue(lockTime))) {
            return true;
        }
        String currentValue = ops.get(key);

        // 如果currentValue为空，说明锁拥有者已经释放掉锁了，可以进行再次尝试
        if (StringUtils.isEmpty(currentValue)) {
            return ops.setIfAbsent(key, getCacheValue(lockTime));
        }

        //判断上一个锁是否到期，到期尝试获取锁
        if (System.currentTimeMillis() >= getExpireTime(currentValue)) {
            //多个线程恰好都到了这里，但是只有一个线程的设置值和当前值相同，它才有权利获取锁
            String oldValue = ops.getAndSet(key, getCacheValue(lockTime));
            return currentValue.equals(oldValue);
        }
        return false;
    }

    /**
     * 尝试获取锁，如果成功，立即返回，如果一直失败，等到超时之后返回
     *
     * @param key     锁key
     * @param timeout 等待时间
     * @param unit    TimeUnit
     * @return boolean
     * @throws InterruptedException InterruptedException
     */
    public boolean tryLock(String key, long timeout, TimeUnit unit) throws InterruptedException {

        long nanosTimeout = unit.toNanos(timeout);

        long lastTime = System.nanoTime();
        do {
            if (tryLock(key)) {
                return true;
            }

            long now = System.nanoTime();
            nanosTimeout -= now - lastTime;
            lastTime = now;

            //响应中断
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        } while (nanosTimeout <= 0L);

        return false;
    }

    /**
     * 获取锁，如果成功，立即返回，如果失败，则不停的尝试，直到成功为止
     *
     * @param key 锁key
     * @throws InterruptedException InterruptedException
     */
    public void lock(String key) throws InterruptedException {

        while (true) {
            if (tryLock(key)) {
                return;
            }

            //响应中断
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
    }


    /**
     * 释放锁
     *
     * @param key key
     */
    public void unlock(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 获取当期线程缓存value
     *
     * @param timeout 超时时间
     * @return 缓存值
     */
    private String getCacheValue(long timeout) {
        return Thread.currentThread().getName() + ";" + String.valueOf(System.currentTimeMillis() + timeout);
    }

    /**
     * 获取过期时间
     *
     * @param cacheValue 缓存值
     * @return 过期时间
     */
    private long getExpireTime(String cacheValue) {
        String[] values = cacheValue.split(";");
        return values.length > 1 ? Long.parseLong(values[1]) : 0L;
    }

    public Set<String> getHkeys(String key){
        //获取所有hash结构中所有的key(hkeys)
        Set<String> keys = redisTemplate.opsForHash().keys(key);
        return keys;
    }
}
