package com.activiti.entity;

public class LeaveEntity {
    private int days;
    private String reason;

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LeaveEntity() {
    }

    @Override
    public String toString() {
        return "LeaveEntity{" +
                "days=" + days +
                ", reason='" + reason + '\'' +
                '}';
    }
}
