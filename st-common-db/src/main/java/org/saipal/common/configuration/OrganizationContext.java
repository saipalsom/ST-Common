package org.saipal.common.configuration;

import org.saipal.common.entity.DataMap;

public class OrganizationContext {
	private static final ThreadLocal<String> currentOrganization = new ThreadLocal<>();
	private static final ThreadLocal<Boolean> isExternal = new ThreadLocal<>();
	private static final ThreadLocal<String> userToken = new ThreadLocal<>();

	private static final ThreadLocal<DataMap> currentUserInfo = new ThreadLocal<>();
	private static String sdkToken;

	public static void setOrganization(String organizationId) {
		currentOrganization.set(organizationId);
	}

	public static String getOrganization() {
		return currentOrganization.get();
	}

	public static void setUserToken(String token) {
		userToken.set(token);
	}

	public static String getUserToken() {
		return userToken.get();
	}

	public static DataMap getCurrentUserinfo() {
		return currentUserInfo.get();
	}

	public static void setCurrentUserInfo(DataMap userInfo) {
		currentUserInfo.set(userInfo);
	}

	public static void clear() {
		currentOrganization.remove();
		isExternal.remove();
		userToken.remove();
	}

	public static void setExternal(Boolean external) {
		isExternal.set(external);
	}

	public static Boolean isExternal() {
		return isExternal.get();
	}

	public static void setSDKToken(String token) {
		sdkToken = token;
	}

	public static String getSdkToken() {
		return sdkToken;
	}



}