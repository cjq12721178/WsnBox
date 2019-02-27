package com.weisi.tool.wsnbox.util;

import android.support.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FlavorClassBuilder {

    public static <I> I buildImplementation(@NonNull Class<I> baseClass) {
        return buildImplementation(baseClass, null, null);
    }

    public static <I> I buildImplementation(@NonNull String implClassName) {
        return buildImplementation(implClassName, null, null);
    }

    public static <I> I buildImplementation(@NonNull Class<I> baseClass, Object... parasAndClasses) {
        if (parasAndClasses == null || parasAndClasses.length % 2 != 0) {
            return buildImplementation(baseClass);
        }
        int length = parasAndClasses.length / 2;
        Class[] paraClasses = new Class[length];
        Object[] paras = new Object[length];
        for (int i = 0;i < length;++i) {
            Object o = parasAndClasses[2 * i];
            if (o instanceof Class) {
                paraClasses[i] = (Class) o;
                paras[i] = parasAndClasses[2 * i + 1];
            } else {
                return buildImplementation(baseClass);
            }
        }
        return buildImplementation(baseClass, paraClasses, paras);
    }

    public static <I> I buildImplementation(Class<I> baseClass, Class[] paraClasses, Object[] paras) {
        return buildImplementation(baseClass.getName() + "Impl", baseClass, paraClasses, paras);
    }

    public static <I> I buildImplementation(@NonNull String implClassName, Class[] paraClasses, Object[] paras) {
        return buildImplementation(implClassName, null, paraClasses, paras);
    }

    public static <I> I buildImplementation(@NonNull String implClassName, Class<I> baseClass, Class[] paraClasses, Object[] paras) {
        try {
            return (I) newInstance(Class.forName(implClassName), paraClasses, paras);
//            if (paraClasses == null || paraClasses.length == 0) {
//                return (I) Class.forName(implClassName).newInstance();
//            }
//            Constructor<I> constructor = (Constructor<I>) Class.forName(implClassName).getConstructor(paraClasses);
//            if (!constructor.isAccessible()) {
//                constructor.setAccessible(true);
//            }
//            return constructor.newInstance(paras);
        } catch (Exception e) {
            try {
                return (I) newInstance(baseClass, paraClasses, paras);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private static Object newInstance(Class target, Class[] paraClasses, Object[] paras)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (paraClasses == null || paraClasses.length == 0 || paras == null || paras.length == 0) {
            return target.newInstance();
        }
        Constructor constructor = target.getConstructor(paraClasses);
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        return constructor.newInstance(paras);
    }
}
