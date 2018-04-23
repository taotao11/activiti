package com.activiti.entity;

import java.io.Serializable;

/**
 * 测试用户类
 *
 */
public class UserEntity implements Serializable{
    /**
     * 虚拟化版本控制
     */
    private static final long serialVersionUID = -6111729763793000176L;
    private int id;
    private String name;

    public UserEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
