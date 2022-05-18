package com.ctg.test;

import com.alibaba.fastjson.JSONObject;
import com.ctg.test.config.FastJson2JsonRedisSerializer;
import com.ctg.test.model.User;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhujunkai
 * @date 2021年09月26日 10:22
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisApplicationTests {

    private final Logger log = LoggerFactory.getLogger(RedisApplicationTests.class);

    @Bean("ivdgRedisTemplate")
    @SuppressWarnings(value = {"unchecked", "rawtypes", "deprecation"})
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);

        template.setValueSerializer(serializer);
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Autowired
    @Qualifier("ivdgRedisTemplate")
    RedisTemplate redisTemplate;

    @Test
    public void testHash(){
        String redisKey = "system_config:SYS_ORG";
        Object org = redisTemplate.opsForHash().get(redisKey, "460291000000");

//        Map<String,Object> sysOrgMap = new HashMap<>();
//        User user = new User();
//        user.setUserName("test");
//        sysOrgMap.put("460100000000",user);
//        redisTemplate.opsForHash().putAll(redisKey,sysOrgMap);
//        Map<String, Objects> redisKeyEntries = redisTemplate.opsForHash().entries(redisKey);

        //设置生成key value的序列化策略
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);

        String key = "hash";
        Map<String,String> map = new HashMap<>();
        map.put("f1","val1");
        map.put("f2","val2");
        //为hash结构设置多个键值对(hmset)
        redisTemplate.opsForHash().putAll(key,map);
        //获取hash结构指定字段的value，单个key(hget)
        Object f1 = redisTemplate.opsForHash().get(key, "f1");
        log.info("f1========"+f1);
        //为hash结构设置单个键值对(hset)
        redisTemplate.opsForHash().put(key,"f3","val3");
        Object f3 = redisTemplate.opsForHash().get(key, "f3");
        log.info("f3========"+f3);
        //判断hash结构中是否包含某字段(hexists)
        Boolean haskey = redisTemplate.opsForHash().hasKey(key, "f3");
        log.info("haskey========"+haskey);
        //获取hash结构中所有的键值对(hgetall)
        Map<String,String> entries = redisTemplate.opsForHash().entries(key);
        for(Map.Entry<String,String> entry : entries.entrySet()){
            log.info(entry.getKey()+"======"+entry.getValue());
        }
        //获取所有hash结构中所有的key(hkeys)
        Set<String> keys = redisTemplate.opsForHash().keys(key);
        for(String key1 : keys){
            log.info("key1=========="+keys);
        }
        //获取hash结构中所有的value(hvals)
        List<String> values = redisTemplate.opsForHash().values(key);
        for(String value : values){
            log.info("value======="+value);
        }
        //获取hash结构中指定key的value，可以是多个key（hmget）
        List<String> list = redisTemplate.opsForHash().multiGet(key, keys);
        for(String value2 : list){
            log.info("value2======="+value2);
        }
        //hash结构中若存在相应key才进行操作(hsetnx)
        Boolean success = redisTemplate.opsForHash().putIfAbsent(key, "f3", "6");
        log.info("succes======"+success);
        //删除hash结构中指定的key(hdel)
        Long delete = redisTemplate.opsForHash().delete(key, "f1", "f2");
        log.info("delete=========="+delete);
    }

    @Test
    public void streamTest() {
        List<User> list = new ArrayList<>();
        User result1 = new User();
        result1.setUserName("37010300001310677602");
        result1.setRealName("1");
        list.add(result1);
        User result2 = new User();
        result2.setUserName("37010300001310015821");
        result2.setRealName("2");
        list.add(result2);
        User result3 = new User();
        result3.setUserName("37013700041310027492");
        result3.setRealName("1");
        list.add(result3);
        User result4 = new User();
        result4.setUserName("37010300001310015821");
        result4.setRealName("3");
        list.add(result4);
        User result5 = new User();
        result5.setUserName("37010300001310677602");
        result5.setRealName("1");
        list.add(result5);
        //获取不合格的设备ID集合
        Set<String> realNameList = list.stream().filter(resultVo->resultVo.getRealName().equals("2") || resultVo.getRealName().equals("3")).map(User::getUserName).collect(Collectors.toSet());
        log.info("realNameList:{}", JSONObject.toJSONString(realNameList));
        //获取检测合格的结果
        list = list.stream().filter(resultVo->!resultVo.getRealName().equals("2") && !resultVo.getRealName().equals("3")).collect(Collectors.toList());
        log.info("list:{}", JSONObject.toJSONString(list));
    }
}