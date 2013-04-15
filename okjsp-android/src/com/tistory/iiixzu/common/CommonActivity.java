package com.tistory.iiixzu.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

/**
 * 공통 Activity.
 * 클래스를 얻어오는 부분이 존재하여 
 * 이 클래스를 상속받아 만드는 클래스에서는 별도로 해당 객체를 얻는작업을 추가할 필요가 없다.
 * 
 * @author iiixzu
 */
public abstract class CommonActivity extends Activity {
	
	private ProgressDialog loadingDialog;
	/**
	 * 다른쓰레드가 UI조작을 할경우에 대한 처리를 위한 Handler
	 */
	private Handler handler;
	
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 
		 handler=new Handler();
	}
	
	protected void startLoadingDialog(int id){
		String message=this.getString(id);
		
		startLoadingDialog(message);
	}

	protected void startLoadingDialog(final String msg){
		handler.post(
				new Runnable(){
					public void run(){
						loadingDialog = new ProgressDialog(CommonActivity.this);
						loadingDialog.setCancelable(false);
						loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						loadingDialog.setMessage(msg);
						loadingDialog.show();
					}
				});
	}
	protected void stopLoadingDialog(){
		
		handler.post(
				new Runnable(){
					public void run(){
						loadingDialog.dismiss();
					}
				});
	}
	/**
	 * 별도의 딜리게이트 처리가 없는 단순 확인창을 띄운다.
	 * 
	 * @param title
	 * @param message
	 * @param 확인버튼명
	 * @param 뒤로가기활성화
	 */
	protected void 안내창띄우기(final String title,final String message,final String 확인버튼명,final boolean 뒤로가기활성화){
		handler.post(new Runnable(){
			public void run(){
				AlertDialog.Builder ab=new AlertDialog.Builder(CommonActivity.this);
				ab.setTitle(title);
			    ab.setMessage(message);
		    	ab.setPositiveButton(확인버튼명,null);
		    	//뒤로가기버튼 안먹게수정..
		     	ab.setCancelable(뒤로가기활성화);	
		    	ab.show();
			}
		});
	}
	protected Handler getHandler(){
		return handler;
	}
}
