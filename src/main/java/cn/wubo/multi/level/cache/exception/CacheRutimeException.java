package cn.wubo.multi.level.cache.exception;

public class CacheRutimeException extends RuntimeException{

    public CacheRutimeException(String message) {
        super(message);
    }

    public CacheRutimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
