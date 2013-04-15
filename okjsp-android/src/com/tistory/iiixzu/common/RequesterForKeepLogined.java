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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class RequesterForKeepLogined {
	
	private IRequest2Observer observer;
	private boolean async;
	private HttpClient httpClient;
	
	private IConfig config;
	
	public RequesterForKeepLogined(IConfig config,HttpClient httpClient,IRequest2Observer observer){
		this.httpClient=httpClient;
		this.observer=observer;
		async=true;
		this.config=config;
	}
	public void submitWithParameters(String urlPath,Map<String,String> paramMap){
		
		String path="http://"+config.getServerName()+"/"+urlPath+"/";
		
		ArrayList parameters = new ArrayList();
		Iterator<String> iter=paramMap.keySet().iterator();
		
		Log.i("Request2",path);
		
		String key=null;
		String value=null;
		while(iter.hasNext()){
			key=iter.next();
			value=paramMap.get(key);
			parameters.add(new BasicNameValuePair(key,value));
			
			Log.i("Request key,value",key+","+value);
		}
		
		BufferedReader reader=null;
		String resultString=null;
		try {
			//파라미터셋팅..
			HttpPost httpPost = new HttpPost(path);
			UrlEncodedFormEntity entity =new UrlEncodedFormEntity(parameters, "UTF-8");
			httpPost.setEntity(entity);
			
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
	public boolean isAsync(){
		return async;
	}
	public void setAsync(boolean isAsync){
		async=isAsync;
	}
	class ExecuteMethod extends Thread{
		
		HttpPost httpPost;
		Handler handler;
		public ExecuteMethod(HttpPost httpPost,Handler handler){
			this.httpPost=httpPost;
			this.handler=handler;
		}
		public void run(){
			try{
				httpClient.execute(httpPost,handler);
			}catch(Exception e){
				
			}
		}
	}
	/**
	 * 비동기 처리시 수행할 로직..
	 * @author iiixzu
	 *
	 */
	class Handler implements ResponseHandler{
		RequesterForKeepLogined request;
		Handler(RequesterForKeepLogined request){
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
			    
				//옵져버호출..
			    if(observer!=null){
			    	observer.finish(request,resultString);
			    }
			    return resultString;
			} catch (IOException e) {
				 //옵져버호출..
			    if(observer!=null){
			    	observer.fail(request);
			    }
			    return resultString;
			} catch (Exception e){
				 //옵져버호출..
			    if(observer!=null){
			    	observer.fail(request);
			    }
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
