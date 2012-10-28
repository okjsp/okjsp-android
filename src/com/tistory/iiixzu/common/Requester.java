package com.tistory.iiixzu.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.Message;
import android.util.Log;

public class Requester implements IRequest2Observer{
	private IRequestObserver observer;
	private boolean async; //비동기통신을 할것인지여부..default YES
	private RequesterForKeepLogined reLoginRequest; //로긴실패시 1회시도하기위한 용도
	
	private static HttpClient httpClient;
	private Map<String,String>sourceParamMap; //파라미터를 저장해놓는 변수..
	private String urlPath; //urlpath를 저장해놓는변수..
	
	private RequestAsyncHandler asyncHandler; //비동기처리핸들러..
	
	private String message; //에러시메세지등..
	private IConfig config;
	
	private boolean isLoading;
	private boolean isMultipart;
	private boolean 로그인체크여부; //로긴체크를 할건지 말건지여부 default=true
	
	//paging
	private int page;
	private boolean isPaging;
	private boolean endPage;
	private boolean isError;
	private String pagingUrlPath;
	static{
		if(httpClient==null){
			httpClient=new DefaultHttpClient();
			
			HttpParams params = httpClient.getParams();  
			
			//타임아웃 30초..
			HttpConnectionParams.setConnectionTimeout(params, 30000);
			HttpConnectionParams.setSoTimeout(params, 30000);
		}
	}
	public Requester(IConfig config){
		this.config=config;
		this.observer=null;
		async=false;
		로그인체크여부=true;
		
		reLoginRequest=new RequesterForKeepLogined(config,httpClient,this);
	}
	public Requester(IConfig config,IRequestObserver observer){
		this.config=config;
		this.observer=observer;
		async=false;
		로그인체크여부=true;
		
		reLoginRequest=new RequesterForKeepLogined(config,httpClient,this);
	}
	/**
	 * 에러등이 발생했을경우 참조메세지이다.
	 * @return
	 */
	public String getMessage(){
		return this.message;
	}
	/**
	 * 로그인체크로직을 태울것인지 말것인지 여부.
	 * @param 로그인체크여부
	 */
	public void setCheckingLogin(boolean 로그인체크여부){
		this.로그인체크여부=로그인체크여부;
	}
	public boolean isAsync(){
		return async;
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
		
		reLoginRequest.setAsync(async);
	}
	public void submitWithParameters(String urlPath,Map<String,String> paramMap){
		
		isLoading=true;
		isError=false;
		isMultipart=false;
		
		sourceParamMap=paramMap;
		this.urlPath=urlPath;
		message=null;
		
		String path="http://"+config.getServerName()+"/"+urlPath;
		
		ArrayList parameters = new ArrayList();
		Log.i("Request",path);
		
		if(sourceParamMap!=null){
			
			Iterator<String> iter=paramMap.keySet().iterator();
			
			String key=null;
			String value=null;
			while(iter.hasNext()){
				key=iter.next();
				value=paramMap.get(key);
				parameters.add(new BasicNameValuePair(key,value));
				
				Log.i("Request key,value",key+","+value);
			}
		}
		BufferedReader reader=null;
		String resultString=null;
		try {
			
			HttpPost httpPost = new HttpPost(path); 
            
			if(sourceParamMap!=null){
				//파라미터셋팅..
				//UrlEncodedFormEntity entity =new UrlEncodedFormEntity(parameters, "UTF-8");
				UrlEncodedFormEntity entity =new UrlEncodedFormEntity(parameters, "ISO-8859-1");
				httpPost.setEntity(entity);
			}
			//실행후 응답받음..
			Log.i("Request","execute start");
			if(this.async){ //비동기호출..
				new ExecuteMethod(httpPost,new Handler(this)).start();
			}else{
				HttpResponse response = httpClient.execute(httpPost);
				
				isLoading=false;
				isError=false;
				
				Log.i("Request","execute end");
			    HttpEntity entityResponse = response.getEntity();
			    reader = new BufferedReader(new InputStreamReader(entityResponse.getContent(), "UTF-8"), 8);
			    
			    String line = null;
			    StringBuilder sb = new StringBuilder();
			    while ((line = reader.readLine()) != null) {
			        sb.append(line);
			    }
			    resultString = sb.toString();
			    
			    //로긴된상태라면 로긴확인처리를 하고
		        //타임아웃된상태라면 재로긴처리한다.
		        //로긴되지않은상태라면 그대로 진행한다.
		        if(config.isLogin()&&this.로그인체크여부){
		            //회원가입 존재여부로 판단한다.
		        	int range1=resultString.indexOf("<a href=\"/html5/member/agreement.jsp\">");
		        	int range2=resultString.indexOf("<a href='/jsp/member/agreement.jsp'");
		            
		            if (range1!=-1&&range2!=-1) {
		                //성공했을경우 delegate호출..
		            	//옵져버호출..
					    if(observer!=null){
					    	observer.finish(this,resultString);
					    }
		            }else {//로그아웃된경우..
		                
		            	config.setLogin(false);
		            			
		            	Map<String,String>parameter=new HashMap<String,String>();
		                parameter.put("id",config.getUserId());
		                parameter.put("password",config.getUserPassword());
		                
		                reLoginRequest.submitWithParameters("html5/member/login2.jsp",parameter);
		            }
		        }else{
		        	//옵져버호출..
				    if(observer!=null){
				    	observer.finish(this,resultString);
				    }
		        }
			}
		} catch (IOException e) {
			
			isLoading=false;
			isError=true;
			
			 //옵져버호출..
		    if(observer!=null){
		    	observer.fail(this);
		    }
		} catch (Exception e){
			
			isLoading=false;
			isError=true;
			
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
	public void submitWithParameters(String urlPath,Map<String,String> paramMap,int page){
		
		this.page=page;
		this.isPaging=true;
		this.sourceParamMap=paramMap;
	    this.pagingUrlPath=urlPath;
	    
	    paramMap.put("pg",this.page+"");
		this.submitWithParameters(urlPath, paramMap);
	}
	//단 페이지가 이미 다 된경우에는 호출해도 진행하지 않는다.
	public void nextPage(){
		
		if(isPaging){
			
			if (!endPage) {
	            
	            int tmpPage;
	            //에러일경우엔 페이지를 증가시키지않는다.
	            if (isError) {
	                tmpPage=page;
	            }else{
	                tmpPage=page+1;
	            }
			
				//if(tmpPage<=totalPage){ //페이징해도되는경우임..
				this.submitWithParameters(this.pagingUrlPath,this.sourceParamMap,tmpPage);
				//}
			}
		}
	}
	public void submitWithParameters(String urlPath,Map<String,String> paramMap,File file){
		isLoading=true;
		isError=false;
		isMultipart=true;
		
		sourceParamMap=paramMap;
		this.urlPath=urlPath;
		message=null;
		
		String path="http://"+config.getServerName()+"/"+urlPath;
		
		Log.i("Request",path);
		
		try {
			
			HttpPost httpPost = new HttpPost(path); 
			
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	        if(sourceParamMap!=null){
				
				Iterator<String> iter=paramMap.keySet().iterator();
				
				String key=null;
				String value=null;
				while(iter.hasNext()){
					key=iter.next();
					value=paramMap.get(key);
					reqEntity.addPart(key,new StringBody(value,Charset.forName("EUC-KR")));
				}
			}
	        
	        if(file!=null){
	        	reqEntity.addPart("myFile", new FileBody(file));
	        }
	        
	        //httpPost.addHeader("charset", "euc-kr");
	        //httpPost.setHeader("Content-Type", "multipart/form-data" );
	        httpPost.setEntity(reqEntity);
	        
			//실행후 응답받음..
			Log.i("Request","execute start");
			if(this.async){ //비동기호출..
				new ExecuteMethod(httpPost,new Handler(this)).start();
			}
		}catch(Exception e){
			
		}
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
				Log.e("Request run",e.toString(),e);
				
				isError=true;
				
				if(observer!=null){
					observer.fail(Requester.this);
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
		Requester request;
		Handler(Requester request){
			this.request=request;
		}
		
		public String handleResponse (HttpResponse response){
			BufferedReader reader=null;
			String resultString=null;
			try {
				
				isLoading=false;
				isError=false;
				
				HttpEntity entityResponse = response.getEntity();
			    reader = new BufferedReader(new InputStreamReader(entityResponse.getContent(), "UTF-8"), 8);
			    
			    String line = null;
			    StringBuilder sb = new StringBuilder();
			    while ((line = reader.readLine()) != null) {
			        sb.append(line);
			    }
			    resultString = sb.toString();
			    
			    //로긴된상태라면 로긴확인처리를 하고
		        //타임아웃된상태라면 재로긴처리한다.
		        //로긴되지않은상태라면 그대로 진행한다.
			    if(config.isLogin()&&Requester.this.로그인체크여부){
				    
		            //회원가입 존재여부로 판단한다.
		        	int range1=resultString.indexOf("<a href=\"/html5/member/agreement.jsp\">");
		        	int range2=resultString.indexOf("<a href='/jsp/member/agreement.jsp'");
		            
		            if (range1==-1&&range2==-1) {
		                //성공했을경우 delegate호출..
		            	//옵져버호출..
		            	Message msg=new Message();
					    msg.what=1;
					    msg.obj=resultString;
					    asyncHandler.sendMessageAtFrontOfQueue(msg);
		            }else {//로그아웃된경우..
		                
		            	config.setLogin(false);
		            	
		            	Map<String,String>parameter=new HashMap<String,String>();
		                parameter.put("id",config.getUserId());
		                parameter.put("password",config.getUserPassword());
		                
		                reLoginRequest.submitWithParameters("html5/member/login2.jsp",parameter);
		            }
		        }else{
		        	Message msg=new Message();
				    msg.what=1;
				    msg.obj=resultString;
				    asyncHandler.sendMessageAtFrontOfQueue(msg);
		        }
		        
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
	
	/**
	 * 요청이 성공했을때 호출됨.
	 * @param request 어떤 Request객체인지.
	 * @param result 결과값.
	 */
	public void finish(RequesterForKeepLogined request,String result){
		try{
			//로긴성공시..
		    //회원가입 존재여부로 판단한다.
			int range1=result.indexOf("<a href=\"/html5/member/agreement.jsp\">");
        	int range2=result.indexOf("<a href='/jsp/member/agreement.jsp'");
		    
        	if (range1==-1&&range2==-1) {
        		config.setLogin(true);
        		
		        //로긴이 성공한경우 다시 원래 호출하려던 파라미터로 재호출..
        		if(this.isMultipart){
        			this.submitWithParameters(urlPath,sourceParamMap,null);
        		}else{
        			this.submitWithParameters(urlPath,sourceParamMap);
        		}
		    }else {
		    	if(this.async){
					this.message="로그아웃되었습니다. 비밀번호가 바뀌었거나 네트웍문제일 수 있습니다.";
					
					Message msg=new Message();
				    msg.what=0;
				    asyncHandler.sendMessageAtFrontOfQueue(msg);
				}else{
					
					isError=true;
					
				    if(observer!=null){
				    	observer.fail(this);
				    }
				}
			}
		    
		}catch(Exception e){ 
			 //옵져버호출..
			if(this.async){
				this.message=e.toString();
				
				Message msg=new Message();
			    msg.what=0;
			    asyncHandler.sendMessageAtFrontOfQueue(msg);
			}else{
				
				isError=true;
				
			    if(observer!=null){
			    	observer.fail(this);
			    }
			}
		}
	}
	/**
	 * 요청이 실패했을때 호출됨.
	 * @param request 어떤 Request객체인지.
	 */
	public void fail(RequesterForKeepLogined request){
		 //옵져버호출..
		if(this.async){
			Message msg=new Message();
		    msg.what=0;
		    asyncHandler.sendMessageAtFrontOfQueue(msg);
		}else{
			
			isError=true;
			
		    if(observer!=null){
		    	observer.fail(this);
		    }
		}
	}
	public boolean isLoading(){
		return this.isLoading;
	}
	public boolean isError(){
		return this.isError;
	}
	public boolean isEndPage(){
		return this.endPage;
	}
	public void setEndPage(boolean endPage){
		this.endPage=endPage;
	}
	/**
	 * 작업을 중단한다. 네트웍연결해제등.
	 */
	public void destroy(){
		//httpClient.getConnectionManager().closeExpiredConnections();
		httpClient.getConnectionManager().shutdown();
	}
}
