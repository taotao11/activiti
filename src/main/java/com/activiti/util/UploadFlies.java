package com.activiti.util;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 文件操作
 */
public class UploadFlies {
    //保存文件
    public static void uploadFile(byte[] file, String filePath, String fileName) throws Exception {
        File targetFile = new File(filePath);
        if(!targetFile.exists()){
            targetFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(filePath+fileName);
        out.write(file);
        out.flush();
        out.close();
    }
}
