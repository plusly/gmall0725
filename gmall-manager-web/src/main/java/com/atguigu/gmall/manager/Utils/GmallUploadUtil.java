package com.atguigu.gmall.manager.Utils;

import com.atguigu.gmall.Constant;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class GmallUploadUtil {

    public static String uploadImg(MultipartFile multipartFile){
        String path = GmallUploadUtil.class.getResource("/tracker.conf").getPath();

        try {
            ClientGlobal.init(path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = null;
        try {
            connection = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StorageClient storageClient = new StorageClient(connection, null);

        String originalFilename = multipartFile.getOriginalFilename();
        String[] split = originalFilename.split("\\.");
        String ext = split[1];

        String[] uploadFile = new String[0];
        try {
            uploadFile = storageClient.upload_file(multipartFile.getBytes(), ext, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        String group = uploadFile[0];
        String adress = uploadFile[1];

        String url = Constant.LINUX_URI + group + "/" + adress;

        return url;
    }
}
