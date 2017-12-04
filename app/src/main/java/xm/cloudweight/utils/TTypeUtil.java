package xm.cloudweight.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import xm.cloudweight.api.ApiSubscribe;

/**
 * @author wyh
 * @Description: 用于获取泛型type
 * @creat 2017/10/31
 */
public class TTypeUtil {


    public static <T> T getTObject(Object o, int i) {
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

    public static Type getTListWithObject(ApiSubscribe clazz){
        return ((ParameterizedType) clazz.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

}
