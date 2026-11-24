package com.sky.findjob;

import lombok.Data;

@Data
public class Result {
    private int code;
    private String message;
    private Object data;
    public static Result success() {
        Result result = new Result();
        result.code = 1;
        result.message = "success";

        return result;
    }
    public static Result success(Object data) {
        Result result = new Result();
        result.code = 1;
        result.message = "success";
        result.data = data;
        return result;
    }
    public static Result fail(String message) {
        Result result = new Result();
        result.code = 0;
        result.message = message;
        return result;
    }
}
