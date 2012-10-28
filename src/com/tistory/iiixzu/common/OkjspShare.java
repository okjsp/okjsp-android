package com.tistory.iiixzu.common;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class OkjspShare implements IConfig{
	
	private static OkjspShare share;
	private HashMap parameterMap;
	private boolean isLogin;
	private String userId;
	private String password;
	private String nickName;
	private Context context;
	private boolean 자동로그인여부;
	private boolean 뒤로가기제스쳐;
	
	private OkjspShare(Context context){
		parameterMap=new HashMap();
		this.context=context;
		load();
	}
	public static OkjspShare getInstance(Context context){
		if(share==null){
			share=new OkjspShare(context);
		}
		
		return share;
	}
	@Override
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId){
		this.userId=userId;
	}
	@Override
	public String getUserPassword() {
		return password;
	}
	public void setUserPassword(String password){
		this.password=password;
	}
	@Override
	public String getServerName() {
		return "www.okjsp.pe.kr";
	}
	public String getNickName(){
		return nickName;
	}
	public void setNickName(String nickName){
		this.nickName=nickName;
	}
	@Override
	public boolean isLogin(){
		return isLogin;
	}
	@Override
	public void setLogin(boolean isLogin){
		this.isLogin=isLogin;
	}
	@Override
	public HashMap<String, Object> getParameterDic() {
		return parameterMap;
	}
	public boolean isAutoLogin(){
		return 자동로그인여부;
	}
	public boolean isGoBackGesture(){
		return 뒤로가기제스쳐;
	}
	/**
	 * 정보를 읽어온다.
	 */
	public void load(){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		
		this.userId=decrypt(pref.getString("아이디",""));
		this.password=decrypt(pref.getString("암호",""));
        this.nickName=pref.getString("닉네임","");
        this.자동로그인여부=pref.getBoolean("자동로그인여부",false);
        this.뒤로가기제스쳐=pref.getBoolean("뒤로가기제스쳐",false);
	}
	/**
	 * 정보를 저장한다.
	 */
	public void commit(){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor=pref.edit();
		
		editor.putString("아이디",encrypt(this.userId));
		editor.putString("암호",encrypt(this.password));
		editor.putString("닉네임",this.nickName);
		editor.putBoolean("자동로그인여부",this.자동로그인여부);
		editor.putBoolean("뒤로가기제스쳐",this.뒤로가기제스쳐);
		
		editor.commit();
	}
	/**
	 * 암호화처리를 한다.
	 * @param text
	 * @param password
	 * @throws Exception
	 */
	private String encrypt(String text) {
		try{
			Key key = generateKey("AES", ByteUtils.toBytes("696d697373796f7568616e6765656e61", 16));
			String transformation = "AES/ECB/PKCS5Padding";
			Cipher cipher = Cipher.getInstance(transformation);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			 
			byte[] hexText=text.getBytes();
			hexText = cipher.doFinal(hexText);
			 
			return ByteUtils.toHexString(hexText);
		}catch(Exception e){
			return "";
		}
	}
	/**
	 * 암호화된데이터를 복호화처리한다.
	 * @param text
	 * @return
	 * @throws Exception
	 */
	private String decrypt(String text){
		try{
			Key key = generateKey("AES", ByteUtils.toBytes("696d697373796f7568616e6765656e61", 16));
			String transformation = "AES/ECB/PKCS5Padding";
			Cipher cipher = Cipher.getInstance(transformation);
			cipher.init(Cipher.DECRYPT_MODE, key);
			 
			byte[] hexId=ByteUtils.toBytesFromHexString(text);
			hexId = cipher.doFinal(hexId);
			 
			return new String(hexId);
		}catch(Exception e){
			return "";
		}
	}
	/**
	  * <p>해당 알고리즘에 사용할 비밀키(SecretKey)를 생성한다.</p>
	  *
	  * @return 비밀키(SecretKey)
	  */
	private Key generateKey(String algorithm) throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
	    SecretKey key = keyGenerator.generateKey();
	    return key;
	    
	}
	
	/**
	* <p>주어진 데이터로, 해당 알고리즘에 사용할 비밀키(SecretKey)를 생성한다.</p>
	*
	* @param algorithm DES/DESede/TripleDES/AES
	* @param keyData
	* @return
	*/
	private Key generateKey(String algorithm, byte[] keyData) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		String upper = algorithm.toUpperCase();
		
		if ("DES".equals(upper)) {
			KeySpec keySpec = new DESKeySpec(keyData);
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
			SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
			return secretKey;
		} else if ("DESede".equals(upper) || "TripleDES".equals(upper)) {
			KeySpec keySpec = new DESedeKeySpec(keyData);
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
			SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
			return secretKey;
		} else {
			SecretKeySpec keySpec = new SecretKeySpec(keyData, algorithm);
			return keySpec;
		}
	}
}
