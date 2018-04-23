package com.activiti.entity;

/**
 * 流程定义实体
 */
public class Definittion {
    //流程定义的ID
    private String defId;
    //流程定义的名称
    private String defName;
    //流程定义的Key
    private String defKey;
    //流程定义的部署ID
    private String demId;
    //流程定义的资源名称
    private String resourceName;
    //流程定义的版本
    private int version;
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }



    public Definittion() {

    }

    public String getDefId() {
        return defId;
    }

    public void setDefId(String defId) {
        this.defId = defId;
    }

    public String getDefName() {
        return defName;
    }

    public void setDefName(String defName) {
        this.defName = defName;
    }

    public String getDefKey() {
        return defKey;
    }

    public void setDefKey(String defKey) {
        this.defKey = defKey;
    }

    public String getDemId() {
        return demId;
    }

    public void setDemId(String demId) {
        this.demId = demId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }


}
