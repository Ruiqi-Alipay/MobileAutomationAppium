package com.alipay.autotest.mobile.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONObject;

public class HttpUtils {

	public static String sendGet(String url, String param) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString = url + (param == null ? "" : "?" + param);
			URL realUrl = new URL(urlNameString);

			URLConnection connection = realUrl.openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect();

			Map<String, List<String>> map = connection.getHeaderFields();
			for (String key : map.keySet()) {
				System.out.println(key + "--->" + map.get(key));
			}

			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送GET请求出现异常！" + e);
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		return result;
	}
	
	public String createTradeOrder(String cashAmount, String couponAmount, String loadtest) {
		try {
			String jsonString = null;

			String imapUrl = "http://imapi-1-64.test.alipay.net";

			String totalAmount = String
					.valueOf((Double.parseDouble(cashAmount) + Double
							.parseDouble(couponAmount)));

			String buyerAdminSeq = "201501080012";
			String buyerCardNo = "21882000000000D1";
			String buyerLoginId = "201501080012";
			String sellerAdminSeq = "201501080011";
			String sellerCardNo = "21882000000000B9";
			String sellerLoginId = "10000tests0011";

			String threadNum = "";

			String partner = "21884000000000B6";
			String service = "ipay.itrade.escrow.create";
			String prodCode = "IPAY_ESCROW_LOADTEST";
			String partnerSubType = "208800000B";

			String activityIds = "98,99";
			if (loadtest != null) {
				activityIds = loadtest;
			}

			Map<String, String> params = new HashMap<String, String>();
			params.put("partner", partner);
			params.put("service", service);
			params.put("charset", "UTF-8");
			params.put("prod_code", prodCode);
			params.put("format", "json");
			params.put("request_dt",
					"" + System.currentTimeMillis());

			if (activityIds.equals("loadtest")) {
				params.put("markuid", "8D");
			}

			params.put("expireTime", "");

			Random rd = new Random();
			String randStr = String.valueOf(System.currentTimeMillis());
			String ordTot = randStr;
			String orderNo = threadNum + randStr + rd.nextInt(10000);

			String tradeOrderList = "";

			if (cashAmount.equals("0")) {
				tradeOrderList = "[ { \"buyerCardNo\": \"buyerCardNoValue\", \"buyerShowName\": \"databank buyer\", \"createDt\": \"2014-07-08 00:00:30.000|US/Pacific\", \"expireDt\": \"ordTotValue\", \"tradeMainType\": \"AE_COMMON\", \"site\": \"ru\", \"safeStockTime\": \"1406803438000\", \"activityIds\": \"activityIdsValue\", \"goodsItemList\": [ { \"rootCategory\": \"12\", \"subCategories\": \"34/56\", \"count\": \"1\", \"itemNo\": \"62900102424272\", \"price\": \"221.49\", \"priceCur\": \"USD\", \"title\": \"2014 New girls sandals kids boots children rivets pu shoes 4colors casual sandals for 2-10 years girls free shipping\", \"unit\": \"piece\" } ], \"intentPayAmount\": \"7855.35\", \"intentPayCur\": \"RUB\", \"logisticInfo\": { \"address1\": \"Molodegnaya 1/15\", \"address2\": \"\", \"contactPerson\": \"apitest buyer\", \"faxNo\": \"--\", \"mobileNo\": \"89022024650\", \"phoneNo\": \"46-3812-89022024650\", \"postCode\": \"644505\", \"shippingCity\": \"Omsk\", \"shippingCountry\": \"RU\", \"shippingFee\": \"0.0\", \"shippingFeeCur\": \"USD\", \"shippingState\": \"Omsk Oblast\", \"shippingMethod\": \"CPAM\", \"shippingExt\": { \"CPF\": \"02367591911\" } }, \"originAmount\": \"originAmountValue\", \"originAmountCur\": \"USD\", \"partnerBuyerId\": \"buyerAdminSeqValue\", \"partnerOrderNo\": \"partnerOrderNoValue\", \"partnerSellerId\": \"sellerAdminSeqValue\", \"partnerSubType\": \"partnerSubTypeValue\", \"preDefPayBillList\": [ { \"direction\": \"OUT\", \"participant\": \"AE_COUPON\", \"tradeAmount\": \"couponAmtValue\", \"tradeAmountCur\": \"USD\" } ], \"riskData\": \"{\\\"billToCity\\\":\\\"Omsk\\\",\\\"billToCountry\\\":\\\"RU\\\",\\\"billToEmail\\\":\\\"helloipay@ipay.com\\\",\\\"billToPhoneNumber\\\":\\\"46381289022024650\\\",\\\"billToPostalCode\\\":\\\"644505\\\",\\\"billToState\\\":\\\"Omsk Oblast\\\",\\\"billToStreet1\\\":\\\"Molodegnaya 1/15\\\",\\\"billToStreet2\\\":\\\"\\\",\\\"buyerAdminSeq\\\":\\\"buyerAdminSeqValue\\\",\\\"buyerLoginId\\\":\\\"buyerLoginIdValue\\\",\\\"buyerSeq\\\":\\\"buyerAdminSeqValue\\\",\\\"category2\\\":\\\"200000947\\\",\\\"category3\\\":\\\"200001003\\\",\\\"categoryLeaf\\\":\\\"200001003\\\",\\\"categoryRoot\\\":\\\"322\\\",\\\"item0ProductName\\\":\\\"2014 New girls sandals kids boots children rivets pu shoes 4colors casual sandals for 2-10 years girls free shipping\\\",\\\"item0Quantiry\\\":\\\"1\\\",\\\"item0UnitPrice\\\":\\\"22149\\\",\\\"logisticsAmount\\\":\\\"0\\\",\\\"mobileNo\\\":\\\"89022024650\\\",\\\"outOrderId\\\":\\\"62900102424272\\\",\\\"outRef\\\":\\\"62900102424272\\\",\\\"productId\\\":\\\"1912777748\\\",\\\"sellerAdminSeq\\\":\\\"sellerAdminSeqValue\\\",\\\"sellerLoginId\\\":\\\"sellerLoginIdValue\\\",\\\"sellerSeq\\\":\\\"sellerAdminSeqValue\\\",\\\"shipToCity\\\":\\\"Omsk\\\",\\\"shipToContactName\\\":\\\"apitest buyer\\\",\\\"shipToCountry\\\":\\\"RU\\\",\\\"shipToPhoneNumber\\\":\\\"46381289022024650\\\",\\\"shipToPostalCode\\\":\\\"644505\\\",\\\"shipToShippingMethod\\\":\\\"CPAM\\\",\\\"shipToState\\\":\\\"Omsk Oblast\\\",\\\"shipToStreet1\\\":\\\"Molodegnaya 1/15\\\",\\\"shipToStreet2\\\":\\\"\\\",\\\"transactionTime\\\":\\\"2014-07-08 00:00:30\\\",\\\"buyerServiceType\\\":\\\"IFM\\\",\\\"sellerServiceType\\\":\\\"CGS\\\",\\\"osType\\\":\\\"WINDOWS\\\"}\", \"sellerCardNo\": \"sellerCardNoValue\", \"sellerShowName\": \"databank seller\", \"tradeAmount\": \"totalAmountValue\", \"tradeAmountCur\": \"USD\", \"tradeChannel\": \"pc\", \"tradeRule\": { \"payChannelRule\": { \"supportPaymentType\": \"all\" } } } ]";

			} else if (couponAmount.equals("0")) {
				tradeOrderList = "[ { \"buyerCardNo\": \"buyerCardNoValue\", \"buyerShowName\": \"databank buyer\", \"createDt\": \"2014-07-08 00:00:30.000|US/Pacific\", \"expireDt\": \"ordTotValue\", \"tradeMainType\": \"AE_COMMON\", \"site\": \"ru\", \"safeStockTime\": \"1406803438000\", \"activityIds\": \"activityIdsValue\", \"goodsItemList\": [ { \"rootCategory\": \"12\", \"subCategories\": \"34/56\", \"count\": \"1\", \"itemNo\": \"62900102424272\", \"price\": \"221.49\", \"priceCur\": \"USD\", \"title\": \"2014 New girls sandals kids boots children rivets pu shoes 4colors casual sandals for 2-10 years girls free shipping\", \"unit\": \"piece\" } ], \"intentPayAmount\": \"7855.35\", \"intentPayCur\": \"RUB\", \"logisticInfo\": { \"address1\": \"Molodegnaya 1/15\", \"address2\": \"\", \"contactPerson\": \"apitest buyer\", \"faxNo\": \"--\", \"mobileNo\": \"89022024650\", \"phoneNo\": \"46-3812-89022024650\", \"postCode\": \"644505\", \"shippingCity\": \"Omsk\", \"shippingCountry\": \"RU\", \"shippingFee\": \"0.0\", \"shippingFeeCur\": \"USD\", \"shippingState\": \"Omsk Oblast\", \"shippingMethod\": \"CPAM\", \"shippingExt\": { \"CPF\": \"02367591911\" } }, \"originAmount\": \"originAmountValue\", \"originAmountCur\": \"USD\", \"partnerBuyerId\": \"buyerAdminSeqValue\", \"partnerOrderNo\": \"partnerOrderNoValue\", \"partnerSellerId\": \"sellerAdminSeqValue\", \"partnerSubType\": \"partnerSubTypeValue\", \"preDefPayBillList\": [ { \"direction\": \"OUT\", \"participant\": \"BUYER\", \"tradeAmount\": \"originAmountValue\", \"tradeAmountCur\": \"USD\" } ], \"riskData\": \"{\\\"billToCity\\\":\\\"Omsk\\\",\\\"billToCountry\\\":\\\"RU\\\",\\\"billToEmail\\\":\\\"helloipay@ipay.com\\\",\\\"billToPhoneNumber\\\":\\\"46381289022024650\\\",\\\"billToPostalCode\\\":\\\"644505\\\",\\\"billToState\\\":\\\"Omsk Oblast\\\",\\\"billToStreet1\\\":\\\"Molodegnaya 1/15\\\",\\\"billToStreet2\\\":\\\"\\\",\\\"buyerAdminSeq\\\":\\\"buyerAdminSeqValue\\\",\\\"buyerLoginId\\\":\\\"buyerLoginIdValue\\\",\\\"buyerSeq\\\":\\\"buyerAdminSeqValue\\\",\\\"category2\\\":\\\"200000947\\\",\\\"category3\\\":\\\"200001003\\\",\\\"categoryLeaf\\\":\\\"200001003\\\",\\\"categoryRoot\\\":\\\"322\\\",\\\"item0ProductName\\\":\\\"2014 New girls sandals kids boots children rivets pu shoes 4colors casual sandals for 2-10 years girls free shipping\\\",\\\"item0Quantiry\\\":\\\"1\\\",\\\"item0UnitPrice\\\":\\\"22149\\\",\\\"logisticsAmount\\\":\\\"0\\\",\\\"mobileNo\\\":\\\"89022024650\\\",\\\"outOrderId\\\":\\\"62900102424272\\\",\\\"outRef\\\":\\\"62900102424272\\\",\\\"productId\\\":\\\"1912777748\\\",\\\"sellerAdminSeq\\\":\\\"sellerAdminSeqValue\\\",\\\"sellerLoginId\\\":\\\"sellerLoginIdValue\\\",\\\"sellerSeq\\\":\\\"sellerAdminSeqValue\\\",\\\"shipToCity\\\":\\\"Omsk\\\",\\\"shipToContactName\\\":\\\"apitest buyer\\\",\\\"shipToCountry\\\":\\\"RU\\\",\\\"shipToPhoneNumber\\\":\\\"46381289022024650\\\",\\\"shipToPostalCode\\\":\\\"644505\\\",\\\"shipToShippingMethod\\\":\\\"CPAM\\\",\\\"shipToState\\\":\\\"Omsk Oblast\\\",\\\"shipToStreet1\\\":\\\"Molodegnaya 1/15\\\",\\\"shipToStreet2\\\":\\\"\\\",\\\"transactionTime\\\":\\\"2014-07-08 00:00:30\\\",\\\"buyerServiceType\\\":\\\"IFM\\\",\\\"sellerServiceType\\\":\\\"CGS\\\",\\\"osType\\\":\\\"WINDOWS\\\"}\", \"sellerCardNo\": \"sellerCardNoValue\", \"sellerShowName\": \"databank seller\", \"tradeAmount\": \"totalAmountValue\", \"tradeAmountCur\": \"USD\", \"tradeChannel\": \"pc\", \"tradeRule\": { \"payChannelRule\": { \"supportPaymentType\": \"all\" } } } ]";

			} else {
				tradeOrderList = "[ { \"buyerCardNo\": \"buyerCardNoValue\", \"buyerShowName\": \"databank buyer\", \"createDt\": \"2014-07-08 00:00:30.000|US/Pacific\", \"expireDt\": \"ordTotValue\", \"tradeMainType\": \"AE_COMMON\", \"site\": \"ru\", \"safeStockTime\": \"1406803438000\", \"activityIds\": \"activityIdsValue\", \"goodsItemList\": [ { \"rootCategory\": \"12\", \"subCategories\": \"34/56\", \"count\": \"1\", \"itemNo\": \"62900102424272\", \"price\": \"221.49\", \"priceCur\": \"USD\", \"title\": \"2014 New girls sandals kids boots children rivets pu shoes 4colors casual sandals for 2-10 years girls free shipping\", \"unit\": \"piece\" } ], \"intentPayAmount\": \"7855.35\", \"intentPayCur\": \"RUB\", \"logisticInfo\": { \"address1\": \"Molodegnaya 1/15\", \"address2\": \"\", \"contactPerson\": \"apitest buyer\", \"faxNo\": \"--\", \"mobileNo\": \"89022024650\", \"phoneNo\": \"46-3812-89022024650\", \"postCode\": \"644505\", \"shippingCity\": \"Omsk\", \"shippingCountry\": \"RU\", \"shippingFee\": \"0.0\", \"shippingFeeCur\": \"USD\", \"shippingState\": \"Omsk Oblast\", \"shippingMethod\": \"CPAM\", \"shippingExt\": { \"CPF\": \"02367591911\" } }, \"originAmount\": \"originAmountValue\", \"originAmountCur\": \"USD\", \"partnerBuyerId\": \"buyerAdminSeqValue\", \"partnerOrderNo\": \"partnerOrderNoValue\", \"partnerSellerId\": \"sellerAdminSeqValue\", \"partnerSubType\": \"partnerSubTypeValue\", \"preDefPayBillList\": [ { \"direction\": \"OUT\", \"participant\": \"BUYER\", \"tradeAmount\": \"originAmountValue\", \"tradeAmountCur\": \"USD\" }, { \"direction\": \"OUT\", \"participant\": \"AE_COUPON\", \"tradeAmount\": \"couponAmtValue\", \"tradeAmountCur\": \"USD\" } ], \"riskData\": \"{\\\"billToCity\\\":\\\"Omsk\\\",\\\"billToCountry\\\":\\\"RU\\\",\\\"billToEmail\\\":\\\"helloipay@ipay.com\\\",\\\"billToPhoneNumber\\\":\\\"46381289022024650\\\",\\\"billToPostalCode\\\":\\\"644505\\\",\\\"billToState\\\":\\\"Omsk Oblast\\\",\\\"billToStreet1\\\":\\\"Molodegnaya 1/15\\\",\\\"billToStreet2\\\":\\\"\\\",\\\"buyerAdminSeq\\\":\\\"buyerAdminSeqValue\\\",\\\"buyerLoginId\\\":\\\"buyerLoginIdValue\\\",\\\"buyerSeq\\\":\\\"buyerAdminSeqValue\\\",\\\"category2\\\":\\\"200000947\\\",\\\"category3\\\":\\\"200001003\\\",\\\"categoryLeaf\\\":\\\"200001003\\\",\\\"categoryRoot\\\":\\\"322\\\",\\\"item0ProductName\\\":\\\"2014 New girls sandals kids boots children rivets pu shoes 4colors casual sandals for 2-10 years girls free shipping\\\",\\\"item0Quantiry\\\":\\\"1\\\",\\\"item0UnitPrice\\\":\\\"22149\\\",\\\"logisticsAmount\\\":\\\"0\\\",\\\"mobileNo\\\":\\\"89022024650\\\",\\\"outOrderId\\\":\\\"62900102424272\\\",\\\"outRef\\\":\\\"62900102424272\\\",\\\"productId\\\":\\\"1912777748\\\",\\\"sellerAdminSeq\\\":\\\"sellerAdminSeqValue\\\",\\\"sellerLoginId\\\":\\\"sellerLoginIdValue\\\",\\\"sellerSeq\\\":\\\"sellerAdminSeqValue\\\",\\\"shipToCity\\\":\\\"Omsk\\\",\\\"shipToContactName\\\":\\\"apitest buyer\\\",\\\"shipToCountry\\\":\\\"RU\\\",\\\"shipToPhoneNumber\\\":\\\"46381289022024650\\\",\\\"shipToPostalCode\\\":\\\"644505\\\",\\\"shipToShippingMethod\\\":\\\"CPAM\\\",\\\"shipToState\\\":\\\"Omsk Oblast\\\",\\\"shipToStreet1\\\":\\\"Molodegnaya 1/15\\\",\\\"shipToStreet2\\\":\\\"\\\",\\\"transactionTime\\\":\\\"2014-07-08 00:00:30\\\",\\\"buyerServiceType\\\":\\\"IFM\\\",\\\"sellerServiceType\\\":\\\"CGS\\\",\\\"osType\\\":\\\"WINDOWS\\\"}\", \"sellerCardNo\": \"sellerCardNoValue\", \"sellerShowName\": \"databank seller\", \"tradeAmount\": \"totalAmountValue\", \"tradeAmountCur\": \"USD\", \"tradeChannel\": \"pc\", \"tradeRule\": { \"payChannelRule\": { \"supportPaymentType\": \"all\" } } } ]";
			}

			tradeOrderList = tradeOrderList.replaceAll("partnerOrderNoValue",
					orderNo);

			tradeOrderList = tradeOrderList.replaceAll("buyerCardNoValue",
					buyerCardNo);
			tradeOrderList = tradeOrderList.replaceAll("buyerLoginIdValue",
					buyerLoginId);
			tradeOrderList = tradeOrderList.replaceAll("buyerAdminSeqValue",
					buyerAdminSeq);

			tradeOrderList = tradeOrderList.replaceAll("sellerCardNoValue",
					sellerCardNo);
			tradeOrderList = tradeOrderList.replaceAll("sellerLoginIdValue",
					sellerLoginId);
			tradeOrderList = tradeOrderList.replaceAll("sellerAdminSeqValue",
					sellerAdminSeq);

			tradeOrderList = tradeOrderList.replaceAll("originAmountValue",
					cashAmount);
			tradeOrderList = tradeOrderList.replaceAll("couponAmtValue",
					couponAmount);
			tradeOrderList = tradeOrderList.replaceAll("totalAmountValue",
					totalAmount);
			tradeOrderList = tradeOrderList.replaceAll("partnerSubTypeValue",
					partnerSubType);

			tradeOrderList = tradeOrderList.replaceAll("ordTotValue", ordTot);

			tradeOrderList = tradeOrderList.replaceAll("activityIdsValue",
					activityIds);

			params.put("trade_order_list", tradeOrderList);

			RequestAssembleManager requestAssemberManager = new RequestAssembleManager();
			String url = "";

			url = requestAssemberManager.makeInvokeUrl(imapUrl, params);
			HttpClientUtils client = new HttpClientUtils();

			jsonString = client.executePost(url);

			JSONObject result = new JSONObject(jsonString);
			JSONObject response = result.getJSONObject("alipay").getJSONObject(
					"response");
			if (response.getString("isSuccess").equals("Y")) {
				String tradeNo = response.getJSONArray("tradeInfoList")
						.getJSONObject(0).getString("tradeNo");

				return tradeNo;
			}
		} catch (Exception e) {
			
		}
		
		return null;
	}
}
