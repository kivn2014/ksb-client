package com.ksb.web.openapi.controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ksb.web.openapi.entity.AjaxEntity;

@Controller
@RequestMapping("/wx")
public class WxApiController {

	// private Logger log = LogManager.getLogger(getClass());

	/**
	 * 微信校验
	 * 
	 * @return
	 */
	@RequestMapping("/validate")
	public @ResponseBody String validate(String signature, String timestamp,
			String nonce, String echostr) throws NoSuchAlgorithmException {

		String token = "ksb706";
		String tmpStr = getSHA1(token, timestamp, nonce);

		System.out.println("=====================tmpStr   " + tmpStr);
		System.out.println("---------------------signature   " + signature);

		if (tmpStr.equals(signature)) {
			return echostr;
		} else {
			return null;
		}

	}

	/**
	 * 用SHA1算法生成安全签名
	 * 
	 * @param token
	 *            票据
	 * @param timestamp
	 *            时间戳
	 * @param nonce
	 *            随机字符串
	 * @param encrypt
	 *            密文
	 * @return 安全签名
	 * @throws NoSuchAlgorithmException
	 * @throws AesException
	 */
	public String getSHA1(String token, String timestamp, String nonce)
			throws NoSuchAlgorithmException {
		String[] array = new String[] { token, timestamp, nonce };
		StringBuffer sb = new StringBuffer();
		// 字符串排序
		Arrays.sort(array);

		for (int i = 0; i < array.length; i++) {
			sb.append(array[i]);
		}
		String str = sb.toString();

		// SHA1签名生成
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(str.getBytes());
		byte[] digest = md.digest();

		StringBuffer hexstr = new StringBuffer();
		String shaHex = "";
		for (int i = 0; i < digest.length; i++) {
			shaHex = Integer.toHexString(digest[i] & 0xFF);
			if (shaHex.length() < 2) {
				hexstr.append(0);
			}
			hexstr.append(shaHex);
		}
		return hexstr.toString();
	}

	/**
	 * 登录成功后的首页
	 * 
	 * @return
	 */
	@RequestMapping("/mobile_main")
	public String mobileMainPage() {
		return "mobileapi/mobile_main";
	}

	/**
	 * 执行登录验证
	 * 
	 * @param name
	 * @param password
	 * @return
	 */
	@RequestMapping("/do_mobile_login")
	public @ResponseBody AjaxEntity doLogin(String name, String password) {

		AjaxEntity rsEntity = new AjaxEntity();

		rsEntity.setSuccess(false);
		if (StringUtils.isBlank(name) && StringUtils.isBlank(password)) {
			rsEntity.setErrors("用户名密码为空");
			return rsEntity;
		}

		if (StringUtils.isBlank(name) || StringUtils.isBlank(password)) {
			name = "";
			password = "";
		}

		if (name.equals("3gongli") && password.equals("3gongli")) {
			rsEntity.setSuccess(true);
			rsEntity.setErrors("ok");
			return rsEntity;
		}
		rsEntity.setErrors("用户名或者密码不正确");
		return rsEntity;
	}

}
