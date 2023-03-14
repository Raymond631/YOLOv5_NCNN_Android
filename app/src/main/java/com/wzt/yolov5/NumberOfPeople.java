package com.wzt.yolov5;

/**
 * @Author: Raymond Li
 * @Date: 2023/3/4 20:47
 * @Version 1.0
 */
public class NumberOfPeople {
    private String id;
    private int lastNum;
    private int number;
    private String timeNow;

    public NumberOfPeople()
    {
    }

    public NumberOfPeople(String id, int lastNum, int number, String timeNow) {
        this.id = id;
        this.lastNum = lastNum;
        this.number = number;
        this.timeNow = timeNow;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLastNum() {
        return lastNum;
    }

    public void setLastNum(int lastNum) {
        this.lastNum = lastNum;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTimeNow() {
        return timeNow;
    }

    public void setTimeNow(String timeNow) {
        this.timeNow = timeNow;
    }

    @Override
    public String toString() {
        return "NumberOfPeople{" +
                "id='" + id + '\'' +
                ", lastNum=" + lastNum +
                ", number=" + number +
                ", timeNow='" + timeNow + '\'' +
                '}';
    }
}
