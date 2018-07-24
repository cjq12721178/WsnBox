package com.weisi.tool.wsnbox.util;

import android.support.annotation.NonNull;

public class FlavorClassBuilder {

    public static <I> I buildImplementation(@NonNull Class<I> baseClass) {
        return buildImplementation(baseClass.getName() + "Impl", baseClass);
    }

    public static <I> I buildImplementation(@NonNull String implClassName) {
        return buildImplementation(implClassName, null);
    }

    public static <I> I buildImplementation(@NonNull String implClassName, Class<I> baseClass) {
        try {
            return (I) Class.forName(implClassName).newInstance();
        } catch (Exception e) {
            try {
                return baseClass.newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
