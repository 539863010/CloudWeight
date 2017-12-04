package xm.cloudweight.utils;

import java.lang.reflect.ParameterizedType;

/**
 * Created by wzp on 2017/7/22.
 */

public class Utils {

    public static <T> T getT(Object o, int i) {
        try {
            /**
             * getGenericSuperclass() : 获得带有泛型的父类
             * ParameterizedType ： 参数化类型，即泛型
             * getActualTypeArguments()[] : 获取参数化类型的数组，泛型可能有多个
             */
            return ((Class<T>) ((ParameterizedType) (o.getClass()
                    .getGenericSuperclass())).getActualTypeArguments()[i])
                    .newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return null;
    }

}
