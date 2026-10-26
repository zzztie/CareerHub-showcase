package com.zzztie.careerhub.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zzztie.careerhub.enums.JobStatus;

@TableName("job")
public class Job extends BaseEntity implements Describable {
    private String title;
    private String company;
    private String description;
    private JobStatus status;

    public Job() {
    }

    public Job(Long id, String title, String company, String description) {
        super(id);
        this.title = title;
        this.company = company;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void printInfo(){
        super.printInfo();
        System.out.println("title="+title);
        System.out.println("company="+company);
        System.out.println("description="+description);
    }

    @Override
    public String getEntityType() {
        return "JOB";
    }

    @Override
    public String getSummary(){
        return "Job:"+getTitle()+"at"+getCompany();
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }
}
