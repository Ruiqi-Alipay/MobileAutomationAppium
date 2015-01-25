package com.alipay.autotest.mobile.utils;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;

public class SignatureUtils {

	public static String sign(String unSignText, String signType,
			String signAlgorithm, String privateKey) throws Exception {
		// 取得私钥
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(
				Base64.decodeBase64(privateKey));
		KeyFactory keyFactory = KeyFactory.getInstance(signType);
		// 生成私钥
		PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);
		// 实例化Signature
		Signature signature = Signature.getInstance(signAlgorithm);
		// 初始化Signature
		signature.initSign(priKey);
		// 更新
		signature.update(unSignText.getBytes());
		return Base64.encodeBase64String(signature.sign());
	}
}
