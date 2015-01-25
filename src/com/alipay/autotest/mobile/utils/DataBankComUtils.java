package com.alipay.autotest.mobile.utils;

import java.util.HashMap;
import java.util.Map;

public class DataBankComUtils {

    private static final DataBankAgent agent;
    private static String              dbType;
    // private static String              dbTypeV2;

    static {
        agent = new DataBankAgent("databank.alibaba-inc.com");
        // if (StringUtil.equalsIgnoreCase("dev", PublicParam.ENV)) {
            dbType = "fc";
            // dbTypeV2 = "stable";
        // } else {
        //    dbType = "auto";
        //    dbTypeV2 = "sit";
        // }
    }

    public static String commonMethod(Map<String, String> userParamsMap, String agentId) {

        StringBuffer userParams = new StringBuffer();
        userParams.append("{");
        for (Map.Entry<String, String> entry : userParamsMap.entrySet()) {
            userParams.append("\"" + entry.getKey() + "\":");
            userParams.append("\"" + entry.getValue() + "\",");
        }
        String parms = userParams.substring(0, userParams.length() - 1);
        parms = parms + "}";

        String runInfoIds = agent.callDataBankAgent(agentId, parms);

        return agent.getDataBankResult(runInfoIds);
    }

    public static String creatNewIpayTradeNo(String buyerCardNo, String cashAmount,
                                             String couponAmount) {
        String tradeNo = null;
        String tradeOrderList = "[{\\\"buyerCardNo\\\": \\\"2188205006685382\\\",\\\"buyerShowName\\\": \\\"databank buyer\\\",\\\"createDt\\\": \\\"2014-07-08 00:00:30.000|US/Pacific\\\",\\\"expireDt\\\": \\\"1406530830000\\\",\\\"tradeMainType\\\": \\\"AE_COMMON\\\",\\\"site\\\": \\\"ru\\\",\\\"safeStockTime\\\": \\\"1406803438000\\\",\\\"activityIds\\\": \\\"98,99\\\",     \\\"goodsItemList\\\": [{\\\"rootCategory\\\": \\\"12\\\",\\\"subCategories\\\": \\\"34/56\\\",\\\"count\\\": \\\"1\\\",\\\"itemNo\\\": \\\"62900102424272\\\",\\\"price\\\": \\\"221.49\\\",\\\"priceCur\\\": \\\"USD\\\",\\\"title\\\": \\\"2014 New girls sandals kids boots children rivets pu shoes 4colors casual sandals for 2-10 years girls free shipping\\\",\\\"unit\\\": \\\"piece\\\"}],\\\"intentPayAmount\\\": \\\"7855.35\\\",\\\"intentPayCur\\\": \\\"RUB\\\",\\\"logisticInfo\\\": {\\\"email\\\":\\\"helloipay@ipay.com\\\",\\\"address1\\\": \\\"Molodegnaya 1/15\\\",\\\"address2\\\": \\\"\\\",\\\"contactPerson\\\": \\\"apitest buyer\\\",\\\"faxNo\\\": \\\"--\\\",\\\"mobileNo\\\": \\\"89022024650\\\",\\\"phoneNo\\\": \\\"46-3812-89022024650\\\",\\\"postCode\\\": \\\"644505\\\",\\\"shippingCity\\\": \\\"Omsk\\\",\\\"shippingCountry\\\": \\\"RU\\\",\\\"shippingFee\\\": \\\"0.0\\\",\\\"shippingFeeCur\\\": \\\"USD\\\",\\\"shippingState\\\": \\\"Omsk Oblast\\\",\\\"shippingMethod\\\": \\\"CPAM\\\",\\\"shippingExt\\\": {\\\"CPF\\\": \\\"02367591911\\\"}},\\\"originAmount\\\": \\\"221.49\\\",\\\"originAmountCur\\\": \\\"USD\\\",\\\"partnerBuyerId\\\": \\\"1007825241\\\", \\\"partnerOrderNo\\\": \\\"63311102424272\\\",\\\"partnerSellerId\\\": \\\"1008757982\\\",\\\"partnerSubType\\\": \\\"2088000001\\\",\\\"preDefPayBillList\\\": [{\\\"direction\\\": \\\"OUT\\\",\\\"participant\\\": \\\"BUYER\\\",\\\"tradeAmount\\\": \\\"191.49\\\",\\\"tradeAmountCur\\\": \\\"USD\\\"},{\\\"direction\\\": \\\"OUT\\\",\\\"participant\\\": \\\"AE_COUPON\\\",\\\"tradeAmount\\\": \\\"30\\\",\\\"tradeAmountCur\\\": \\\"USD\\\"}],\\\"riskData\\\": \\\"{\\\\\\\"billToCity\\\\\\\":\\\\\\\"Omsk\\\\\\\",\\\\\\\"billToCountry\\\\\\\":\\\\\\\"RU\\\\\\\",\\\\\\\"billToEmail\\\\\\\":\\\\\\\"helloipay@ipay.com\\\\\\\",\\\\\\\"billToPhoneNumber\\\\\\\":\\\\\\\"46381289022024650\\\\\\\",\\\\\\\"billToPostalCode\\\\\\\":\\\\\\\"644505\\\\\\\",\\\\\\\"billToState\\\\\\\":\\\\\\\"Omsk Oblast\\\\\\\",\\\\\\\"billToStreet1\\\\\\\":\\\\\\\"Molodegnaya 1/15\\\\\\\",\\\\\\\"billToStreet2\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"buyerAdminSeq\\\\\\\":\\\\\\\"1007825241\\\\\\\",\\\\\\\"buyerLoginId\\\\\\\":\\\\\\\"db1007825240\\\\\\\",\\\\\\\"buyerSeq\\\\\\\":\\\\\\\"1007825241\\\\\\\",\\\\\\\"category2\\\\\\\":\\\\\\\"200000947\\\\\\\",\\\\\\\"category3\\\\\\\":\\\\\\\"200001003\\\\\\\",\\\\\\\"categoryLeaf\\\\\\\":\\\\\\\"200001003\\\\\\\",\\\\\\\"categoryRoot\\\\\\\":\\\\\\\"322\\\\\\\",\\\\\\\"item0ProductName\\\\\\\":\\\\\\\"2014 New girls sandals kids boots children rivets pu shoes 4colors casual sandals for 2-10 years girls free shipping\\\\\\\",\\\\\\\"item0Quantiry\\\\\\\":\\\\\\\"1\\\\\\\",\\\\\\\"item0UnitPrice\\\\\\\":\\\\\\\"22149\\\\\\\",\\\\\\\"logisticsAmount\\\\\\\":\\\\\\\"0\\\\\\\",\\\\\\\"mobileNo\\\\\\\":\\\\\\\"89022024650\\\\\\\",\\\\\\\"outOrderId\\\\\\\":\\\\\\\"62900102424272\\\\\\\",\\\\\\\"outRef\\\\\\\":\\\\\\\"62900102424272\\\\\\\",\\\\\\\"productId\\\\\\\":\\\\\\\"1912777748\\\\\\\",\\\\\\\"sellerAdminSeq\\\\\\\":\\\\\\\"1008757982\\\\\\\",\\\\\\\"sellerLoginId\\\\\\\":\\\\\\\"db1008757981\\\\\\\",\\\\\\\"sellerSeq\\\\\\\":\\\\\\\"1008757982\\\\\\\",\\\\\\\"shipToCity\\\\\\\":\\\\\\\"Omsk\\\\\\\",\\\\\\\"shipToContactName\\\\\\\":\\\\\\\"apitest buyer\\\\\\\",\\\\\\\"shipToCountry\\\\\\\":\\\\\\\"RU\\\\\\\",\\\\\\\"shipToPhoneNumber\\\\\\\":\\\\\\\"46381289022024650\\\\\\\",\\\\\\\"shipToPostalCode\\\\\\\":\\\\\\\"644505\\\\\\\",\\\\\\\"shipToShippingMethod\\\\\\\":\\\\\\\"CPAM\\\\\\\",\\\\\\\"shipToState\\\\\\\":\\\\\\\"Omsk Oblast\\\\\\\",\\\\\\\"shipToStreet1\\\\\\\":\\\\\\\"Molodegnaya 1/15\\\\\\\",\\\\\\\"shipToStreet2\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"transactionTime\\\\\\\":\\\\\\\"2014-07-08 00:00:30\\\\\\\",\\\\\\\"buyerServiceType\\\\\\\":\\\\\\\"IFM\\\\\\\",\\\\\\\"sellerServiceType\\\\\\\":\\\\\\\"CGS\\\\\\\",\\\\\\\"osType\\\\\\\":\\\\\\\"WINDOWS\\\\\\\"}\\\",     \\\"sellerCardNo\\\": \\\"2188205002099413\\\",\\\"sellerShowName\\\": \\\"databank seller\\\",\\\"tradeAmount\\\": \\\"221.49\\\",\\\"tradeAmountCur\\\": \\\"USD\\\",  \\\"tradeChannel\\\": \\\"pc\\\",\\\"tradeRule\\\": {\\\"payChannelRule\\\": {\\\"supportPaymentType\\\": \\\"all\\\"}}}]";
        dbChange();
        Map<String, String> param = new HashMap<String, String>();
        param.put("expireTime", "2592000000");
        param.put("trade_channel", "PC");
        param.put("trade_order_list", tradeOrderList);
        param.put("databankServerUrl", "http://databank.intl.alipay.net:8080");
        param.put("prod_code", "DEFAULT_PRODUCT_CODE");
        param.put("env", dbType);
        param.put("trade_cash_amount", cashAmount);
        param.put("trade_coupon_amount", couponAmount);
        param.put("newOrder", "true");
        param.put("partnerSubType", "2088000001");
        param.put("version", "1.0.0");
        param.put("partner", "2188400000000016");
        param.put("sitbuyerCardNo", buyerCardNo);
        param.put("devbuyerCardNo", buyerCardNo);
        param.put("devItradeUrl", "http://itrade.stable.alipay.net:8080");

        String result = commonMethod(param, "2294");
        result = result.split(",")[1];
        tradeNo = result.split("=")[2];

        return tradeNo;
    }

    public static void dbChange() {
        if (dbType.equals("fc") || dbType.equals("dev")) {
            dbType = "dev";
        } else {
            dbType = "sit";
        }
    }

}
