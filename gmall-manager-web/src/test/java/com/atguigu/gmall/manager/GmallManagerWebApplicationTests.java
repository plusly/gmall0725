package com.atguigu.gmall.manager;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManagerWebApplicationTests {

	@Test
	public void contextLoads() throws IOException, MyException {

		String path = GmallManagerWebApplicationTests.class.getResource("/tracker.conf").getPath();

		ClientGlobal.init(path);

		TrackerClient trackerClient = new TrackerClient();
		TrackerServer connection = trackerClient.getConnection();

		StorageClient storageClient = new StorageClient(connection, null);

		String[] uploadFile = storageClient.upload_file("e:/pps.jpg", "jpg", null);

		String group = uploadFile[0];
		String adress = uploadFile[1];

		String url = "http://192.168.41.155/" + group + "/" + adress;
		System.out.print(url);
	}
	@Test
	public void testString(){
		String str = "safdfadg.gad";
		String[] split = str.split(".");
		for (String s : split) {
			System.out.println(s);
		}

	}


}
