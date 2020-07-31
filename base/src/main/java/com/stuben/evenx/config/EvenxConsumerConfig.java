package com.stuben.evenx.config;

public class EvenxConsumerConfig {
    private int threadNum = 5;

    private String mqHost = "127.0.0.1";

    private int mqPort = 9876;

    private String appName = "test";

    private String scanPackage = "com";


    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public String getMqHost() {
        return mqHost;
    }

    public void setMqHost(String mqHost) {
        this.mqHost = mqHost;
    }

    public int getMqPort() {
        return mqPort;
    }

    public void setMqPort(int mqPort) {
        this.mqPort = mqPort;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setScanPackage(String scanPackage) {
        this.scanPackage = scanPackage;
    }

    public String getScanPackage() {
        return scanPackage;
    }
}
