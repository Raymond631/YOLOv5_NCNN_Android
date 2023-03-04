package com.wzt.yolov5;

/**
 * @Author: Raymond Li
 * @Date: 2023/1/30 11:18
 * @Version 1.0
 */
public class Message{

    private int code;//状态码
    private Object msg;//响应信息

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getMsg() {
        return msg;
    }

    public void setMsg(Object msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "Message{" +
                "code=" + code +
                ", msg=" + msg +
                '}';
    }
}
