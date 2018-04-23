package com.activiti.entity;

import java.util.Date;

/**
 * 代办任务实体
 */
public class TaskEntity {
    private String id;
    private String name;
    private Date creatDate;
    private LeaveForm form;

    @Override
    public String toString() {
        return "TaskEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", creatDate='" + creatDate + '\'' +
                ", form=" + form +
                '}';
    }

    public TaskEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatDate() {
        return creatDate;
    }

    public void setCreatDate(Date creatDate) {
        this.creatDate = creatDate;
    }

    public LeaveForm getForm() {
        return form;
    }

    public void setForm(LeaveForm form) {
        this.form = form;
    }
}
