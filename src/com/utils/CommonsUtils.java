package com.utils;

import java.util.UUID;

/**
 * Created by Kuexun on 2018/4/26.
 */
public class CommonsUtils {
    //����uuid
    public static String getUuid()
    {
        return UUID.randomUUID().toString();
    }
}
