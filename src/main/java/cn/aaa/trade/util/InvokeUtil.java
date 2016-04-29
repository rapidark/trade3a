package cn.aaa.trade.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class InvokeUtil {

    public static Object invoke(Object invoker, String cmd, Map params) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Method method = invoker.getClass().getMethod(cmd, Map.class);
        return method.invoke(invoker, params);
    }
}
