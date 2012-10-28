package com.tistory.iiixzu.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.Message;
import android.util.Log;

/**
 * Requester가 특정 모바일서버에 로그인처리후 사용을 위해 구성되어졌다면.
 * 이 클래스는 그런것을 따지지않고 어떠한 url주소로도 호출할수 있게 만들어졌다.
 * ex)구글맵정보를 가져옴,linkapp 버전정보를 가져옴등.
 * 
 * @author iiixzu
 *
 */
public class URLRequester {
	private IURLRequestObserver observer;
	private boolean async; //비동기통신을 할것인지여부..default YES
	
	private static HttpClient httpClient;
	private Map<String,String>sourceParamMap; //파라미터를 저장해놓는 변수..
	
	private RequestAsyncHandler asyncHandler; //비동기처리핸들러..
	
	private String message; //에러시메세지등..
	
	private String method;
	private String getParameters;
	
	static{
		if(httpClient==null){
			httpClient=new DefaultHttpClient();
			
			HttpParams params = httpClient.getParams();  
			
			//타임아웃 5초..
			HttpConnectionParams.setConnectionTimeout(params, 5000);
			HttpConnectionParams.setSoTimeout(params, 5000);
		}
	}
	/**
	 * 기본 POST방식으로 실행되며 다른방식으로 실행하고자 한다면
	 * setMethod메소드를 사용하면 된다.
	 * @param observer
	 */
	public URLRequester(IURLRequestObserver observer){
		this.observer=observer;
		async=false;
		method="POST";
	}
	/**
	 * 에러등이 발생했을경우 참조메세지이다.
	 * @return
	 */
	public String getMessage(){
		return this.message;
	}
	public boolean isAsync(){
		return async;
	}
	/**
	 * 호출 method양식을 결정한다.
	 * @param method
	 */
	public void setMethod(String method){
		this.method=method.toUpperCase();
	}
	/**
	 * 이 메소드를 설정하면 비동기로 데이터가 처리된다.
	 */
	public void setAsync(RequestAsyncHandler handler){
		if(handler==null){
			async=false;
			asyncHandler=null;
		}else{
			async=true;
			asyncHandler=handler;
			asyncHandler.setInfo(this,observer);
		}
	}
	public void submitWithUrl(String urlString,Map<String,String> paramMap){
		
		sourceParamMap=paramMap;
		message=null;
		
		String path=urlString;
		
		ArrayList parameters = new ArrayList();
		
		if(paramMap!=null){
			
			getParameters="";
			
			Iterator<String> iter=paramMap.keySet().iterator();
			String key=null;
			String value=null;
			while(iter.hasNext()){
				key=iter.next();
				value=paramMap.get(key);
				parameters.add(new BasicNameValuePair(key,value));
				
				getParameters+=key+"="+value+"&";
				
				Log.i("Request key,value",key+","+value);
			}
		}
		
		BufferedReader reader=null;
		String resultString=null;
		try {
			
			if(this.method.equals("POST")){
			
				//파라미터셋팅..
				HttpPost httpPost = new HttpPost(path);
				
				if(paramMap!=null){
					UrlEncodedFormEntity entity =new UrlEncodedFormEntity(parameters, "UTF-8");
					httpPost.setEntity(entity);
				}
				//실행후 응답받음..
				Log.i("Request","execute start");
				if(this.async){ //비동기호출..
					new ExecuteMethod(httpPost,new Handler(this)).start();
				}else{
					HttpResponse response = httpClient.execute(httpPost);
					
					Log.i("Request","execute end");
				    HttpEntity entityResponse = response.getEntity();
				    reader = new BufferedReader(new InputStreamReader(entityResponse.getContent(), "UTF-8"), 8);
				    
				    String line = null;
				    StringBuilder sb = new StringBuilder();
				    while ((line = reader.readLine()) != null) {
				        sb.append(line);
				    }
				    resultString = sb.toString();
				    
				    //옵져버호출..
					if(observer!=null){
						observer.finish(this,resultString);
					}
				}
			}else{
				//파라미터셋팅..
				if(paramMap!=null){
					path=path+"?"+getParameters;
				}
				
				HttpGet httpGet = new HttpGet(path);
				
				//실행후 응답받음..
				Log.i("Request","execute start");
				if(this.async){ //비동기호출..
					new ExecuteMethod(httpGet,new Handler(this)).start();
				}else{
					HttpResponse response = httpClient.execute(httpGet);
					
					Log.i("Request","execute end");
				    HttpEntity entityResponse = response.getEntity();
				    reader = new BufferedReader(new InputStreamReader(entityResponse.getContent(), "UTF-8"), 8);
				    
				    String line = null;
				    StringBuilder sb = new StringBuilder();
				    while ((line = reader.readLine()) != null) {
				        sb.append(line);
				    }
				    resultString = sb.toString();
				    
				    //옵져버호출..
					if(observer!=null){
						observer.finish(this,resultString);
					}
				}
			}
		} catch (IOException e) {
			 //옵져버호출..
		    if(observer!=null){
		    	observer.fail(this);
		    }
		} catch (Exception e){
			 //옵져버호출..
		    if(observer!=null){
		    	observer.fail(this);
		    }
		} finally {
			
			try{reader.close();}catch(Exception e){}
			//멤버변수일때 이것을 쓰면 재접속이 불가능하게된다.
			//그래서 주석처리..
			//try{httpClient.getConnectionManager().shutdown();}catch(Exception e){}
		}
	}
	class ExecuteMethod extends Thread{
		
		HttpPost httpPost;
		HttpGet httpGet;
		Handler handler;
		int type;
		
		public ExecuteMethod(HttpPost httpPost,Handler handler){
			this.httpPost=httpPost;
			this.handler=handler;
			
			type=1;
		}
		public ExecuteMethod(HttpGet httpGet,Handler handler){
			this.httpGet=httpGet;
			this.handler=handler;
			
			type=2;
		}
		public void run(){
			try{
				if(type==1){
					httpClient.execute(httpPost,handler);
				}else{
					httpClient.execute(httpGet,handler);
				}
			}catch(Exception e){
				Log.e("Request run",e.toString(),e);
				
				if(observer!=null){
					observer.fail(URLRequester.this);
				}
			}
		}
	}
	/**
	 * 비동기 처리시 수행할 로직..
	 * @author iiixzu
	 *
	 */
	class Handler implements ResponseHandler{
		URLRequester request;
		Handler(URLRequester request){
			this.request=request;
		}
		
		public String handleResponse (HttpResponse response){
			BufferedReader reader=null;
			String resultString=null;
			try {
				HttpEntity entityResponse = response.getEntity();
			    reader = new BufferedReader(new InputStreamReader(entityResponse.getContent(), "UTF-8"), 8);
			    
			    String line = null;
			    StringBuilder sb = new StringBuilder();
			    while ((line = reader.readLine()) != null) {
			        sb.append(line);
			    }
			    resultString = sb.toString();
			    
			    Message msg=new Message();
				msg.what=1;
				msg.obj=resultString;
				asyncHandler.sendMessageAtFrontOfQueue(msg);
				
			    return resultString;
			} catch (Exception e) {
				Message msg=new Message();
			    msg.what=0;
			    asyncHandler.sendMessageAtFrontOfQueue(msg);
			    return resultString;
			} finally {
				
				try{reader.close();}catch(Exception e){}
				//멤버변수일때 이것을 쓰면 재접속이 불가능하게된다.
				//그래서 주석처리..
				//try{httpClient.getConnectionManager().shutdown();}catch(Exception e){}
			}
		}
	}
}
