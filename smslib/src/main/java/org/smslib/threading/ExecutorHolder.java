package org.smslib.threading;

import java.util.concurrent.Executor;

public class ExecutorHolder {

    private static Executor executor;

    public static Executor getExecutor() {
        return executor;
    }

    public static void setExecutor(Executor executor) {
        ExecutorHolder.executor = executor;
    }

}
