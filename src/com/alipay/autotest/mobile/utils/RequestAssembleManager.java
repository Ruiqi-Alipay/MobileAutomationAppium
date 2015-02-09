package com.alipay.autotest.mobile.utils;

import java.net.URLEncoder;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ipay.imapi.util.signature.SignatureUtil;

/**
 * �ⲿ�ӿ�������װ�� ,�������ڲ�����ƽ̨��װ�ⲿ�ӿ��������
 * 
 * @author: agui
 * @date: 2011-1-19
 * @version: $Id: RequestAssembleManager.groovy, v 0.1 2011-1-19 13:03:20 agui Exp $
 */
public class RequestAssembleManager {

    /**
     * ��װ�ӿ���������Url
     * @param requestParam �ӿ��������
     * @param invokeMode �ӿ���������ģʽ����mapi��aliapi��openapi
     * @return ���������ӿ�����url
     */
    public String makeInvokeUrl(String url, Map<String, String> requestParam)
                                                                             throws SignatureException {

        // String gateway = getGatewayAddress(invokeMode);
        String queryString = assemble(requestParam);

        return url + ExterfaceConstants.gateway + queryString;

    }

    /**
     * �ⲿ�ӿ�����queryString��װ
     * @param requestParam �ӿ��������
     * @return String ���ؽӿ�����queryString
     */
    private String assemble(Map<String, String> requestParam) throws SignatureException {

        requestParam.remove(ExterfaceParams.signType);
        requestParam.remove(ExterfaceParams.sign);

        String sign = SignatureUtil.sign(requestParam, ExterfaceConstants.privateKey,
            ExterfaceConstants.signType, ExterfaceConstants.charset);

        requestParam.put(ExterfaceParams.sign, sign);
        requestParam.put(ExterfaceParams.signType, ExterfaceConstants.signType);

        return getQueryString(requestParam);
    }

    /**
     * queryString ��װ
     * @param params ��װ����
     * @return String
     */
    private String getQueryString(Map<String, String> params) {
        if (params == null) {
            return null;
        }
        StringBuffer content = new StringBuffer();
        List<String> keys = new ArrayList(params.keySet());

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            try {

                value = URLEncoder.encode(value, ExterfaceConstants.charset);
            } catch (Exception ex) {

            }
            content.append((i == 0 ? "" : "&") + key + "=" + value);
        }

        return content.toString();
    }

}
