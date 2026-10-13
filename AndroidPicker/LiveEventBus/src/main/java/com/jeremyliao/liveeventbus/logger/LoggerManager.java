package com.jeremyliao.liveeventbus.logger;

import java.util.logging.Level;

/**
 * Created by liaohailiang on 2019-11-07.
 */
public class LoggerManager implements Logger {

    private Logger logger;
    private boolean enable = false;

    public LoggerManager(Logger logger) {
        this.logger = logger;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(Level level, String msg) {
        if (enable) {
            logger.log(level, msg);
        }
    }

    @Override
    public void log(Level level, String msg, Throwable th) {
        if (enable) {
            logger.log(level, msg, th);
        }
    }

    /**
     * 懒求值日志：需要取值的对象以可变参数传入，只有日志开关开启时才执行
     * String.format 按位置取值，关闭日志时不会拼接字符串、不会触发对象的 toString() 调用
     * 若最后一个参数为 Throwable，则按异常日志输出（会打印堆栈）
     *
     * @param level  Log级别
     * @param format 格式串，占位符与后续参数按位置对应
     * @param args   需要取值的参数
     */
    public void log(Level level, String format, Object... args) {
        if (!enable) {
            return;
        }
        Throwable th = null;
        if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
            th = (Throwable) args[args.length - 1];
        }
        String msg = String.format(format, args);
        if (th != null) {
            logger.log(level, msg, th);
        } else {
            logger.log(level, msg);
        }
    }
}
