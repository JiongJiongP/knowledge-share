package com.company.common.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message) {
        this(500, message);
    }

    public static BizException notFound(String entity) {
        return new BizException(404, entity + "不存在");
    }

    public static BizException forbidden() {
        return new BizException(403, "权限不足");
    }

    public static BizException badRequest(String msg) {
        return new BizException(400, msg);
    }
}
