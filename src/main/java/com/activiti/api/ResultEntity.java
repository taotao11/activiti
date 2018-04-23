package com.activiti.api;

/**
 * 统一返回结果实体类
 */
public class ResultEntity {
    private int status;
    private String message;
    private Object data;

    public ResultEntity(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
    //成功返回结果
    public static ResultEntity success(String message,Object data){

        return new ResultEntity(200,message,data);
    }
    public static ResultEntity error(String message){
        return new ResultEntity(500,message,null);
    }
    public ResultEntity() {
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
