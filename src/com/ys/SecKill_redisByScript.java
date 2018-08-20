package com.ys;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.joran.conditional.ElseAction;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.Transaction;


public class SecKill_redisByScript {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecKill_redisByScript.class);
    static String secKillScript = "local userid=KEYS[1];\r\n" +
            "local prodid=KEYS[2];\r\n" +
            "local qtkey='sk:'..prodid..\":qt\";\r\n" +
            "local usersKey='sk:'..prodid..\":usr\";\r\n" +
            "local userExists=redis.call(\"sismember\",usersKey,userid);\r\n" +
            "if tonumber(userExists)==1 then \r\n" +
            "   return 2;\r\n" +
            "end\r\n" +
            "local num= redis.call(\"get\" ,qtkey);\r\n" +
            "if tonumber(num)<=0 then \r\n" +
            "   return 0;\r\n" +
            "else \r\n" +
            "   redis.call(\"decr\",qtkey);\r\n" +
            "   redis.call(\"sadd\",usersKey,userid);\r\n" +
            "end\r\n" +
            "return 1";
    static String secKillScript2 =
            "local userExists=redis.call(\"sismember\",\"{sk}:0101:usr\",userid);\r\n" +
                    " return 1";
    public static boolean doSecKill(String uid, String prodid) throws IOException {
        JedisPool jedispool = JedisPoolUtil.getJedisPoolInstance();
        Jedis jedis = jedispool.getResource();
        //加载脚本
        String sha1 = jedis.scriptLoad(secKillScript);
        Object result = jedis.evalsha(sha1, 2, uid, prodid); //执行脚本
        String reString = String.valueOf(result);
        if ("0".equals(reString)) {
            System.out.println("抢光了！");
        } else if ("1".equals(reString)) {
            System.out.println("抢购完成！");
        } else if ("2".equals(reString)) {
            System.out.println("此人已抢购！");
        } else {
            System.out.println("抢购失败！");
        }
        jedis.close();
        return true;
    }
}
