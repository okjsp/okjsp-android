package com.tistory.iiixzu.common;

import android.os.Handler;
import android.os.Message;

/**
 * 비동기처리용 핸들러.
 * Request객체에 이 객체를 생성해서 넘겨주면 비동기로 처리된다.
 * 단. 이 핸들러는 Main Thread에서 생성해서 넘겨주어야한다.
 * @author iiixzu
 */
public class RequestAsyncHandler extends Handler {
	
	private IRequestObserver observer;
	private Requester request;
	private IURLRequestObserver urlObserver;
	private URLRequester urlRequet;
	
	private int type;
	
	public void handleMessage(Message msg){
		if(msg.what==0){ //실패..
			//옵져버호출..
			if(type==1){
				if(observer!=null){
		    		observer.fail(request);
				}
				
		    }else{
		    	if(urlObserver!=null){
		    		urlObserver.fail(urlRequet);
		    	}
		    }
		}else if(msg.what==1){ //성공..
			
			String resultString=(String)msg.obj;
			//옵져버호출..
			if(type==1){
				if(observer!=null){
					observer.finish(request,resultString);
		    	}
			}else{
				if(urlObserver!=null){
		    		urlObserver.finish(urlRequet,resultString);
		    	}
		    }
		}else{
			//이런경우는 없음~ 'ㅡ')~..루루루..
		}
	}
	/**
	 * 사용자가 설정할필요가 없는 메소드.
	 * 그래서 접근자가 Default이다.
	 * @param observer
	 */
	void setInfo(Requester request,IRequestObserver observer){
		this.request=request;
		this.observer=observer;
		
		type=1;
	}
	/**
	 * 사용자가 설정할필요가 없는 메소드.
	 * 그래서 접근자가 Default이다.
	 * @param observer
	 */
	void setInfo(URLRequester request,IURLRequestObserver observer){
		this.urlRequet=request;
		this.urlObserver=observer;
		
		type=2;
	}
}
