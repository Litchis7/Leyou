package com.leyou.upload.service;



import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {

    /*大小写切换：ctrl+shift+u*/
    private static final List<String> CONTENT_TYPES = Arrays.asList("image/gif","image/jpeg","image/png");

   /* public static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);*/
   private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class); //选org.slf4j.Logger

    @Autowired
    private FastFileStorageClient storageClient;

    public String uploadImage(MultipartFile file) {

        String originalFilename = file.getOriginalFilename();
       /* originalFilename.split(".");   //写法有点low  */
        /*StringUtils.substringAfterLast()*/
        //检验文件类型
        String contentType = file.getContentType();
        if (!CONTENT_TYPES.contains(contentType)){
            LOGGER.info("文件类型不合法：{}", originalFilename);
            return null;
        }
        //CTRL + ALT + T 抛出异常快捷键
        try {
            //校验文件的内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null){
                LOGGER.info("文件内容不合法：{}", originalFilename);
                return null;
            }

            //保存到服务器,不要忘记在路径下面加\\
            /*
            //保存到本地
            file.transferTo(new File("F:\\Study\\leyou\\upload-image\\"+ originalFilename));
            */
            String ext =  StringUtils.substringAfterLast(originalFilename,".");
            StorePath storePath = this.storageClient.uploadFile(file.getInputStream(),file.getSize(),ext,null);

            //返回url，进行回显
            /*
            return "http://image.leyou.com/"+originalFilename;
            */

            return "http://image.leyou.com/" +storePath.getFullPath();


        } catch (IOException e) {
           // LOGGER.info("服务器内部错误：{}", originalFilename);
            LOGGER.info("服务器内部错误:"+originalFilename);
            e.printStackTrace();
        }
        return null;
    }
}
