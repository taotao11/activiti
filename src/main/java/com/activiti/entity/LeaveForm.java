package com.activiti.entity;

import java.util.Date;

public class LeaveForm {
    //任务id
    private String id;
    //申请人
    private String name;
    private int days;
    private String yy;
    private Date date;
    private String status;

    //审核人
    private String message;
    private String auditor;
    private Date auditTime;

    private Date endLeaveTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getEndLeaveTime() {
        return endLeaveTime;
    }

    public void setEndLeaveTime(Date endLeaveTime) {
        this.endLeaveTime = endLeaveTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuditor() {
        return auditor;
    }

    public void setAuditor(String auditor) {
        this.auditor = auditor;
    }

    public Date getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(Date auditTime) {
        this.auditTime = new Date(auditTime.getTime());
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getYy() {
        return yy;
    }

    public void setYy(String yy) {
        this.yy = yy;
    }

    public LeaveForm() {
    }

    @Override
    public String toString() {
        return "LeaveForm{" +
                "name='" + name + '\'' +
                ", days=" + days +
                ", yy='" + yy + '\'' +
                '}';
    }
}
