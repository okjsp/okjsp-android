package com.tistory.iiixzu.okjsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.pe.theeye.remoteimagedownloader.ImageDownloader;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tistory.iiixzu.common.AdamAdViewDelegater;
import com.tistory.iiixzu.common.CommonActivity;
import com.tistory.iiixzu.common.IRequestObserver;
import com.tistory.iiixzu.common.OkjspShare;
import com.tistory.iiixzu.common.RequestAsyncHandler;
import com.tistory.iiixzu.common.Requester;
import com.tistory.iiixzu.common.StringHelper;
import com.tistory.iiixzu.common.SwipeDetector;
/**
 * 게시물목록.
 * @author iiixzu
 *
 */
public class Board extends CommonActivity implements IRequestObserver{
	
	private Requester requester;
	private ImageButton 갱신버튼;
	private boolean isRefresh;
	private boolean isFirstLoaded;
	
	private ListView lv게시물목록;
	private Adapter 아답터;
	private ArrayList<Map<String,String>> list;
	private int pageNum;
	private boolean isWritable;
	private HtmlCleaner xpathParser;
	
	//뒤로가기제스쳐위한방법..
	private SwipeDetector swipeDetector;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board);
        
        갱신버튼=(ImageButton)this.findViewById(R.id.갱신);
        
        Map<String,String> boardData=(HashMap<String,String>)OkjspShare.getInstance(this).getParameterDic().get("boardData");
        TextView tv게시판제목=(TextView)this.findViewById(R.id.게시판제목);
        tv게시판제목.setText(boardData.get("menuName"));
        
        list=new ArrayList<Map<String,String>>();
        
        lv게시물목록=(ListView)this.findViewById(R.id.게시물목록);
        
        if(OkjspShare.getInstance(this).isGoBackGesture()){
        	swipeDetector=new SwipeDetector();
        	lv게시물목록.setOnTouchListener(swipeDetector);
        }
        
        아답터=new Adapter();
        lv게시물목록.setAdapter(아답터);
        lv게시물목록.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,  int position, long id){
				if(swipeDetector!=null&&swipeDetector.swipeDetected()){
					if(swipeDetector.getAction()==SwipeDetector.Action.LR){
						Board.this.finish();
					}
				}else{
					if(list.size()==position){//다음페이지 클릭일경우..
						
						ProgressBar progressBar=(ProgressBar)view.findViewById(R.id.progressBar);
			        	progressBar.setVisibility(View.VISIBLE);
			        	
			    		TextView tv제목=(TextView)view.findViewById(R.id.제목);
						tv제목.setText("로드중...");
						
				        //if (isSearch) {
				            //[requester submitWithParameters:self.searchNextPageLink];
				        //}else{
						Board.this.startLoadingDialog("로드중...");
				            requester.nextPage();
				        //}
					}else{//게시물보기인경우..
						//게시판내용을 얻는다.
						Map<String,String> data=(Map<String,String>)아답터.getItem(position);
				        
						OkjspShare.getInstance(Board.this).getParameterDic().put("contentData",data);
				        
				        //댓글갯수가 일정갯수 이상일경우 파싱에 문제가 생기므로
				        //이때는 일반 웹뷰로 로딩한다.
				        String reply=data.get("reply");
				        reply=reply.replace("[","");
				        reply=reply.replace("]","");
				        
				        int replyCount=Integer.parseInt(reply);
				        
				        if (replyCount>99) {
				        	Toast.makeText(Board.this,"댓글수 99개 초과", Toast.LENGTH_SHORT);
				            //웹브라우져를 띄운다..
				        	
				        }else{
				        	Intent intent=new Intent(Board.this, Content.class);
				        	startActivityForResult(intent,100);
				        }
					}
				}
			}
        	
        });
        xpathParser=new HtmlCleaner();
        
        this.refresh(갱신버튼);
	}
	@Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if(requestCode==100&&resultCode==100){ //글이 수정된 경우 다시리프레쉬..
    		아답터.notifyDataSetChanged();
    	}else if(requestCode==100&&resultCode==200){ //글이 삭제된 경우 해당데이터를 삭제후 다시 리프래쉬..
    		
    		Object obj=OkjspShare.getInstance(Board.this).getParameterDic().get("contentData");
    		list.remove(obj);
    		
    		아답터.notifyDataSetChanged();
    	}
    }
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		if(OkjspShare.getInstance(this).isLogin()){
			menu.add(android.view.Menu.NONE,1, 0, "글쓰기");
			return true;
		}else{
			menu.add(android.view.Menu.NONE,2, 0, "권한이 없습니다.(로그인하십시오)");
			return true;
		}
	}
	@Override
	public boolean onPrepareOptionsMenu (android.view.Menu menu){
			
		menu.clear();
		
		if(OkjspShare.getInstance(this).isLogin()){
			menu.add(android.view.Menu.NONE,1, 0, "글쓰기");
			return true;
		}else{
			menu.add(android.view.Menu.NONE,2, 0, "권한이 없습니다.(로그인하십시오)");
			return true;
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if(item.getItemId()==1){//글쓰기.
			startActivityForResult(new Intent(this, WriteContent.class),100);
			overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
		}
		return (super.onOptionsItemSelected(item));
	}
	//refresh
	public void refresh(View sender){
		//버튼이 터치된상태라면 작업이 완료되기전까지
		//재실행이 금지된다.--연속터치시 에러발생...
		갱신버튼.setEnabled(false);
		
		isRefresh=true;
		isFirstLoaded=false;
	    
		Map<String,String> boardData=(HashMap<String,String>)OkjspShare.getInstance(this).getParameterDic().get("boardData");
		Map<String,String>parameter=new HashMap<String,String>();
        parameter.put("act","LIST");
        parameter.put("bbs",boardData.get("boardId"));
        
        this.startLoadingDialog("로드중...");
        
        requester=new Requester(OkjspShare.getInstance(this),this);
        requester.setEndPage(false);
        requester.setAsync(new RequestAsyncHandler());
        requester.setCheckingLogin(false);
        requester.submitWithParameters("html5/bbs/list_result.jsp",parameter,0);
	}
	//데이터를 잘라온다.
	private List<String> checkContent(String result){
	    
		String[] tmp=result.split("<article>");
		ArrayList<String> list=new ArrayList<String>();
		
		for(String data : tmp){
			list.add(data);
		}
		list.remove(0);
	    
	    return list;
	}
	@Override
	public void finish(Requester request, String result) {
	        
		this.stopLoadingDialog();
		
        //중복데이터관리를위한 임시객체
        ArrayList<Map<String,String>> tmpList=new ArrayList<Map<String,String>>();
        boolean isAddThread=false;
        
        if (isRefresh) {
        	list.clear();
            
            //맨위로 옮긴다
        	lv게시물목록.post(new Runnable() {            
                @Override
                public void run() {
                	lv게시물목록.setSelection(0);
                	//lv게시물목록.setSelectionFromTop(0,0);
                }
            });
        	
            isRefresh=false;
        }
        
        List<String> arrData=this.checkContent(result);
        
        Map<String,String> threadData=null;
        String nick=null;
        String nickImage=null;
        String subject=null;
        String link=null;
        String reply=null;
        String shortWriteDate=null;
        String writeDate=null;
        String readCount=null;
        String tmp=null;
        
        TagNode tagNode=null;
        for (int i=0;i<arrData.size();i++) {
        	
            tmp=arrData.get(i);
            tmp="<article>"+tmp;
            
            threadData=new HashMap<String,String>();
            
            tagNode=xpathParser.clean(tmp);
            
            writeDate=this.getTagData(tagNode,"//article/h3/text()");
            nick=this.getTagData(tagNode,"//article/h3/a/text()");
            nickImage=this.getTagData(tagNode,"//article/h3/img/@src");
            subject=this.getTagData(tagNode,"//article/h4/a/text()");
            link=this.getTagData(tagNode,"//article/h4/a/@href");
            reply=this.getTagData(tagNode,"//article/p[@class='details']/a[3]/text()");
            
            String[] tmpReadCount=tmp.split("<p class=\"details\">");
            //XML QnA게시판같은경우 게시물내용내에 <article>등의 데이터가 
            //섞여있어서 문제를 야기한다.
            //그냥 이런데이터는 이렇게 처리해서 걸러내는것으로 바꾼다.
            if (subject.equals("")||tmpReadCount.length!=2) {
                continue;
            }
            readCount=tmpReadCount[1];
            readCount=StringHelper.tokenize(readCount,"|")[4];
            readCount=readCount.replace("Read:","");
            readCount=readCount.trim();
            
            link="html5/"+link;
            //작성일가공.
            shortWriteDate=writeDate.substring(0,writeDate.indexOf("("));
            shortWriteDate=shortWriteDate.trim();
            writeDate=writeDate.substring(0,writeDate.indexOf(")"));
            writeDate=writeDate.substring(writeDate.indexOf("(")+1,writeDate.length());
            
            threadData.put("shortWriteDate",shortWriteDate);
            threadData.put("writeDate",writeDate);
            threadData.put("nick",nick);
            threadData.put("nickImage",nickImage);
            threadData.put("subject",subject);
            threadData.put("link",link);
            threadData.put("readCount",readCount);
            threadData.put("reply","["+reply+"]");
            
            String contentNo=StringHelper.tokenize(link,"/")[2];
            threadData.put("contentNo",contentNo);

            //내용을 뽑아낸다.
            tmp=StringHelper.tokenize(tmp,"<section>")[1];
            tmp=StringHelper.tokenize(tmp,"</section>")[0];
            threadData.put("content",tmp);
            
            if(this.isExistThead(threadData)){
            	tmpList.add(threadData);
            }else{
                isAddThread=true;
                list.add(threadData);
            }
        }
        
        //만일데이터가 1개도 포함되지 않았다면 
        //한페이지 이상 넘겨진것으로 간주하고..
        //그냥 데이터를 모조리 추가시킨다.
        if(!isAddThread){
        	list.removeAll(tmpList);
        }
        
        request.setEndPage(this.isLastPage(result));
        
        //페이지당글의 갯수(페이징산정및 글쓰기권한존재여부확인용)
        //여기서 얻어진 수량정보와 얻어진 게시물정보의 수량이 맞지않으면 마지막 페이지로 간주한다.
        //처음한번만 처리한다..
        if (!isFirstLoaded) {
            //첫번째 페이지의 수량을 기준으로 페이지당 라인수를 체크한다.
            pageNum=arrData.size();
            //로긴된 사람이라면 쓰기권한이 있는것으로 간주한다.
            isWritable=OkjspShare.getInstance(this).isLogin();
            isFirstLoaded=true;
        }
        
        아답터.notifyDataSetChanged();
        //처리된 item을 다시이용가능하게한다.
        갱신버튼.setEnabled(true);
	}
	@Override
	public void fail(Requester request) {
		
		this.stopLoadingDialog();
        
        this.getHandler().post(new Runnable(){
        	public void run(){
        		
        		//처리된 item을 다시이용가능하게한다.
        		갱신버튼.setEnabled(true);
        		
        		아답터.notifyDataSetChanged();
        		
        		AlertDialog.Builder ab=new AlertDialog.Builder(Board.this);
        		ab.setTitle("오류");
        	    ab.setMessage("연결상태를 확인후 재시도 바랍니다.");
            	ab.setPositiveButton("확인",null);
            	//뒤로가기버튼 안먹게수정..
             	//ab.setCancelable(false);	
            	ab.show();
        	}
        });
	}
	//게시물이 이미 존재하는지 체크..
	private boolean isExistThead(Map<String,String> data){
		Map<String,String> tmpData;
	    
	    //제목으로 체크하면 
	    //게시물작성자가 제목을 수정하는 상황이 발생했을시
	    //제대로 대응안되는 문제가 있으나..
	    //게시물번호를 얻어오는것이 블럭처리되거나 공지사항게시물의경우
	    //문제를 일으킬수있기때문에 이렇게 처리한다..
	    String subject1=data.get("subject");
	    String nick1=data.get("nick");
	    String subject2=null;
	    String nick2=null;
	    
	    int count=list.size()-1;
	    for(int i=count;i>=0;i--){
	        tmpData=list.get(i);
	        //존재하는지 체크..
	        subject2=tmpData.get("subject");
	        nick2=tmpData.get("nick");
	        
	        if(subject1.equals(subject2)&&nick1.equals(nick2)){
	            return true;
	        }
	    }
	    
	    return false;
	}
	//마지막페이지인지 여부를 체크한다.
	/*
	 <input type='button' class='button_two' id='nextBtn' value='다음페이지' onclick="this.style.display='none';getList('html5', '2')" />
	*/
	private boolean isLastPage(String pData){
		try{
			TagNode tagNode=xpathParser.clean(pData);
			Object[] ttt=tagNode.evaluateXPath("//input[@id='nextBtn']/@value");
			if(ttt==null){
				return true;
			}else if(ttt.length>0){
				if(ttt[0].toString().equals("다음페이지")){
					return false;
				}else{
					return true;
				}
			}else{
				return true;
			}
		}catch(Exception e){
			return false;
		}
	}
	/**
	 * 태그의 내용정보를 추출하여 리턴한다.
	 * @param tagNode
	 * @param xPath
	 * @return
	 */
	private String getTagData(TagNode tagNode,String xPath){
		try{
			Object[] ttt=tagNode.evaluateXPath(xPath);
			if(ttt==null){
				return "";
			}else if(ttt.length>0){
				return ttt[0].toString();
			}else{
				return "";
			}
			
		}catch(Exception e){
			return "";
		}
	}
	/**
	 * 게시판목록 아답터..
	 * @author iiixzu
	 */
	public class Adapter extends BaseAdapter {
			
        public Adapter(){
        	
        }
        
        public int getCount(){
        	return list.size()+2;
        }
        
        public Object getItem(int index){
        	return list.get(index);
        }
        public long getItemId(int index){
        	return index;
        }
        
        public View getView(int position,View convertView,ViewGroup parent){
        	
        	if(position==list.size()+1){//광고..	
        		LayoutInflater inflater=getLayoutInflater();
        		LinearLayout layout=(LinearLayout)inflater.inflate(R.layout.ad,null);
        		layout.addView(AdamAdViewDelegater.getInatance(Board.this).getAdView());
        		
        		return layout;
        	}else if(position==list.size()){ //다음페이지 뷰..
        		LayoutInflater inflater=getLayoutInflater();
	        	convertView=(LinearLayout)inflater.inflate(R.layout.board_item_next,null);
	        	
	        	ProgressBar progressBar=(ProgressBar)convertView.findViewById(R.id.progressBar);
	        	progressBar.setVisibility(View.GONE);
	        	
	    		TextView tv제목=(TextView)convertView.findViewById(R.id.제목);
	    		
	    		if (requester.isEndPage()) {
	    			//cell.userInteractionEnabled=NO;
	    			//label.textColor=[UIColor lightGrayColor];
	                //label.highlightedTextColor=[UIColor whiteColor];
	    			
	    			convertView.setOnTouchListener(new View.OnTouchListener() {                    
                        public boolean onTouch(View v, MotionEvent event) {
                            // 여기서 이벤트를 막습니다.
                            return true;
                        }
                    });
	    			
	                tv제목.setText("마지막입니다.");
	    		}else {
	                
	                if (requester.isError()) {
	                    //cell.userInteractionEnabled=YES;
	                    //label.textColor=[UIColor lightGrayColor];
	                    //label.highlightedTextColor=[UIColor whiteColor];
	                    //[cell setSelectionStyle:UITableViewCellSelectionStyleBlue];

	                    tv제목.setText("로드하지 못했습니다.");
	                }else if (requester.isLoading()) {
	    				//[activity startAnimating];
	    				//cell.userInteractionEnabled=NO;
	    				//label.textColor=[UIColor lightGrayColor];
	                    //label.highlightedTextColor=[UIColor whiteColor];
	                	convertView.setEnabled(false);
			        	progressBar.setVisibility(View.VISIBLE);
	    				tv제목.setText("로드중...");
	    			}else {
	    				
	    				if (list.size()==0) {
	    					//label.textColor=[UIColor lightGrayColor];
	                        //label.highlightedTextColor=[UIColor whiteColor];
	    					
	                        //if (isSearch) {
	                            //cell.userInteractionEnabled=YES;
	                            //tv제목.setText("다음검색...");
	                        //}else{
	                            //cell.userInteractionEnabled=NO;
	                            //[activity startAnimating];
	    						convertView.setEnabled(false);
	                            tv제목.setText("로드중...");
	                        //}
	    				}else {
	                        
	                        //cell.userInteractionEnabled=YES;
	                        //label.textColor=[UIColor darkGrayColor];
	                        //label.highlightedTextColor=[UIColor whiteColor];
	                        //[cell setSelectionStyle:UITableViewCellSelectionStyleBlue];
	                        
	                        //if (isSearch) {
	                            //tv제목.setText("다음검색...");
	                        //}else{
	    						convertView.setEnabled(true);
	                            tv제목.setText(pageNum+"개 더 보기...");
	                        //}
	    				}
	    			}
	    		}
        	}else{
	        	ItemHolder holder=null;
	        	if (convertView == null || convertView.getId() != R.layout.board_item) {
		        	LayoutInflater inflater=getLayoutInflater();
		        	convertView=(LinearLayout)inflater.inflate(R.layout.board_item,null);
		        	
		        	holder = new ItemHolder();
		        	holder.tv작성자=(TextView)convertView.findViewById(R.id.작성자);
		        	holder.tv제목=(TextView)convertView.findViewById(R.id.제목);
		        	holder.tv리플수=(TextView)convertView.findViewById(R.id.리플수);
		        	holder.iv작성자이미지=(ImageView)convertView.findViewById(R.id.작성자이미지);
		        	
		        	convertView.setTag(holder);
		        	convertView.setId(R.layout.board_item);
	        	}else{
	        		holder = (ItemHolder) convertView.getTag();
	        	}
	        	
	        	if(position%2==0){
					convertView.setBackgroundResource(R.drawable.board_item_color1);			
				}else{
					convertView.setBackgroundResource(R.drawable.board_item_color2);
				}
	        	
	        	Map<String,String> data=list.get(position);
	        	
	        	holder.tv작성자.setText(data.get("nick"));
	        	holder.tv제목.setText(data.get("subject"));
	        	if(data.get("reply").equals("[0]")){
	        		holder.tv리플수.setText("");
	        	}else{
	        		holder.tv리플수.setText(data.get("reply"));
	        	}
	            ImageDownloader.download(data.get("nickImage"), holder.iv작성자이미지);
        	}
        	
        	return convertView;
        }
    }
	class ItemHolder{
		public ImageView iv작성자이미지;
		public TextView tv작성자;
		public TextView tv제목;
		public TextView tv리플수;
	}
}
