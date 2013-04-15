package com.tistory.iiixzu.common;

import java.util.HashMap;

public interface IConfig {
	/**
	 * 아이디정보를 얻는다.
	 * @return
	 */
	public String getUserId();
	/**
	 * 패스워드정보를 얻는다.
	 * @return
	 */
	public String getUserPassword();
	
	/**
	 * 서버 아이피를 얻는다.
	 * @return
	 */
	public String getServerName();
	
	/**
	 * 로그인여부를 얻는다.
	 * @return
	 */
	public boolean isLogin();
	/**
	 * 로그인여부를 저장한다.
	 * @param isLogined
	 */
	public void setLogin(boolean isLogin);
	
	/**
	 * 앱상에서 데이터를 공유할 목적으로 사용하는 공유객체.
	 * @return Hash맵기반의 객체.
	 */
	public HashMap<String,Object> getParameterDic();
}
