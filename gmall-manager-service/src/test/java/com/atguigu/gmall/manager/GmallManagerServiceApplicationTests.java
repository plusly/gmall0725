package com.atguigu.gmall.manager;

import com.atguigu.gmall.util.RedisConfig;
import com.atguigu.gmall.util.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManagerServiceApplicationTests {

	@Autowired
	RedisUtil redisUtil;

	@Test
	public void contextLoads() {
		Jedis jedis = redisUtil.getJedis();

		String ping = jedis.ping();

		System.err.print(ping);

		jedis.close();
	}

}
