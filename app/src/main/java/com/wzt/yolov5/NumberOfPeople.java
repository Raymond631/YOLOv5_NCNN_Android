package com.wzt.yolov5;

/**
 * @Author: Raymond Li
 * @Date: 2023/3/4 20:47
 * @Version 1.0
 */
public class NumberOfPeople {
    private String id;
    private int number;

    public NumberOfPeople()
    {
    }

    public NumberOfPeople(String id, int number)
    {
        this.id = id;
        this.number = number;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "NumberOfPeople{" +
                "id='" + id + '\'' +
                ", number=" + number +
                '}';
    }
}
