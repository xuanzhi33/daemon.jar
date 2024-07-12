package cn.xuanzhi33.daemonjar;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result {
    private int code;
    private String message;

    public static Result success() {
        return new Result(200, "success");
    }
    public static Result error(int code, String message) {
        return new Result(code, message);
    }
    public static Result tokenError() {
        return new Result(403, "Token error");
    }

}
