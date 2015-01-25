/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.alipay.autotest.mobile.utils;

import java.math.BigDecimal;

/**
 * 转换Byte、KB、MB单位
 * @author qiyi.wxc
 * @version $Id: ByteUtil.java, v 0.1 2014年9月19日 下午5:20:12 qiyi.wxc Exp $
 */
public class ByteUtil {
    /**
     * byte转成kb
     * 
     * @param bytes
     * @return
     */
    public static float bytes2Kb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal kilobyte = new BigDecimal(1024);
        return filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP).floatValue();
    }
}
