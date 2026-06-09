package edu.msudenver.cs.replican;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class P38 {
    public static Object call (String methodName, Object o) throws Throwable {
        Method m=o.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);

        try {
            return m.invoke(o);
        } catch (InvocationTargetException IET) {
            throw IET.getCause();
        }
    }

    public static Object call (String methodName, Object o, Object args[]) throws Throwable {
        Class arguments[] = new Class[args.length];

        for (int i = 0; i < args.length; i++) {
            arguments[i] = args[i].getClass();
        }

        Method m = o.getClass().getDeclaredMethod(methodName, arguments);
        m.setAccessible(true);

        try {
            return m.invoke(o, args);
        } catch (InvocationTargetException IET) {
            throw IET.getCause();
        }
    }
}
