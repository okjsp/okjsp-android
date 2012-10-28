package com.tistory.iiixzu.okjsp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import com.tistory.iiixzu.common.OkjspShare;
/**
 * 옥희설정.
 * @author iiixzu
 *
 */
public class Setting extends PreferenceActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.setting);
    }
	
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if((keyCode == KeyEvent.KEYCODE_BACK)){
			
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
	        String 닉네임= pref.getString("닉네임","");
	        닉네임=닉네임.trim();
	        
	        if(닉네임.equals("")){
	        	
	        	//진동주기..
	    		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    		vibe.vibrate(500);
	    		
	        	AlertDialog.Builder ab=new AlertDialog.Builder(this);
	    		ab.setTitle("안내");
	    	    ab.setMessage("닉네임은 필수설정입니다.");
	        	ab.setPositiveButton("확인",null);
	        	//뒤로가기버튼 안먹게수정..
	         	//ab.setCancelable(뒤로가기활성화);	
	        	ab.show();
	        	
	        	return false;
	        }else{
	        	
	        	OkjspShare.getInstance(this).load();
	        	OkjspShare.getInstance(this).setNickName(닉네임);
	        	OkjspShare.getInstance(this).commit();
	        	
	        	this.finish();
	        }
		}
		return true;
	}
}
