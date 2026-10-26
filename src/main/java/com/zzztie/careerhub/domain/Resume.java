package com.zzztie.careerhub.domain;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("resume")
public class Resume extends BaseEntity implements Describable {
    private Long userId;
    private String fileName;
    private String filePath;

    public Resume() {
    }

    public Resume(String filepath, String filename, Long userId, Long id) {
        super(id);
        this.filePath = filepath;
        this.fileName = filename;
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFilename() {
        return fileName;
    }

    public void setFilename(String filename) {
        this.fileName = filename;
    }

    public String getFilepath() {
        return filePath;
    }

    public void setFilepath(String filepath) {
        this.filePath = filepath;
    }

    @Override
    public void printInfo(){
        super.printInfo();
        System.out.println("userId="+userId);
        System.out.println("filename="+fileName);
        System.out.println("filepath="+filePath);
    }

    @Override
    public String getEntityType() {
        return "RESUME";
    }

    @Override
    public String getSummary(){
        return "Resume:"+getFilename()+",userId="+getUserId();
    }

}
