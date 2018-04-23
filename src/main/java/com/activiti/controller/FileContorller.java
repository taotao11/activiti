package com.activiti.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * bpmn文件上传
 */
@RestController
public class FileContorller {
    @RequestMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             HttpServletRequest request){
        System.out.println(file);

        return "success";
    }
}
