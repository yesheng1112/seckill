package com.ys;

import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.List;

public class SecKill_redis {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecKill_redis.class);

    public static boolean doSecKill(String uid, String prodid) throws IOException {
        JedisPool jedisPool = JedisPoolUtil.getJedisPoolInstance();
        Jedis jedis = jedisPool.getResource();

        System.out.println("active:"+jedisPool.getNumActive()+"||"+jedisPool.getNumWaiters());

        String qtkey = "sk:" + prodid + ":qt";
        String usrKey = "sk:" + prodid + ":usr";

        //判断是否已经抢过
        if (jedis.sismember(usrKey,uid)){
            jedis.close();
            System.err.println("已抢过！！！");
            return false;
        }

        //判断是否还有库存
        jedis.watch(qtkey);
        int qt = Integer.parseInt(jedis.get(qtkey));
        if (qt <= 0) {
            jedis.close();
            System.err.println("没有库存！！！");
            return false;
        }
        Transaction transaction = jedis.multi();


        //减库存
        transaction.decr(qtkey);

        //加人
        transaction.sadd(usrKey, uid);
        List<Object> list = transaction.exec();
        if (list == null || list.size()==0){
            System.err.println("抢购失败！！！！！！");
            jedis.close();
            return false;
        }

        jedis.close();
        System.out.println("抢购成功！！！");
        return true;
    }
}
