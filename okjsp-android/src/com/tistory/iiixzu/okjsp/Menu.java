package com.tistory.iiixzu.okjsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.tistory.iiixzu.common.AdamAdViewDelegater;
import com.tistory.iiixzu.common.CommonActivity;
import com.tistory.iiixzu.common.IRequestObserver;
import com.tistory.iiixzu.common.OkjspShare;
import com.tistory.iiixzu.common.RequestAsyncHandler;
import com.tistory.iiixzu.common.Requester;
import com.tistory.iiixzu.common.ShortcutMenu;

/**
 * 메인메뉴.
 * @author iiixzu
 */
public class Menu extends CommonActivity implements IRequestObserver, SensorEventListener {
	
	private boolean 확장여부;
	private 메뉴목록아답터 아답터;
	private ExpandableListView elv메뉴목록;
	
	private boolean isShortCutMode;
	private boolean is즐찾추가;
	private List<Map<String,String>>shortCutMenuList;
	private List<Map<String,String>> menuList;
	private ShortcutMenu shortCutMenu;
	
	private TextView prompt;
	private View 로그인뷰;
	private TextView tv아이디;
	private TextView tv암호;
	private Requester requester;
	private Requester requester2;
	
	private View 바로가기뷰;
	
	//Shake이벤트처리를 위한 변수.
	private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
   
    private float x, y, z;
   
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;
    
	private SensorManager sensorManager;
    private Sensor accelerormeterSensor;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        
        LayoutInflater inflater=getLayoutInflater();
        바로가기뷰=(LinearLayout)inflater.inflate(R.layout.header_view,null);
    	
        elv메뉴목록=(ExpandableListView)this.findViewById(R.id.메뉴목록);
        elv메뉴목록.addHeaderView(바로가기뷰);
        아답터=new 메뉴목록아답터();
		elv메뉴목록.setAdapter(아답터);
		elv메뉴목록.setGroupIndicator(null);
		elv메뉴목록.setOnGroupClickListener(new OnGroupClickListener(){
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,int groupPosition, long id) {
				if(groupPosition==0){
		        	if(확장여부){
		        		ExpandableListView tmpListView=(ExpandableListView)parent;
		            	tmpListView.collapseGroup(groupPosition);
		            	
		        	}else{
		        		ExpandableListView tmpListView=(ExpandableListView)parent;
		            	tmpListView.expandGroup(groupPosition);

		        	}
	        	}else if(groupPosition==1){
	        		//셋팅화면으로 이동한다.
	        		startActivity(new Intent(Menu.this, Setting.class));
	        	}else if(groupPosition==2){ //okjsp트윗..
	        		Intent i=new Intent(Intent.ACTION_VIEW);
	        		Uri uri=Uri.parse("http://www.twitter.com/okjsp");
	        		i.setData(uri);
	        		startActivity(i);
	        	}else if(groupPosition==3){ //문의및 오류신고..
	        		Intent i=new Intent(Intent.ACTION_VIEW);
	        		Uri uri=Uri.parse("http://mobile.twitter.com/iiixzu");
	        		i.setData(uri);
	        		startActivity(i);
	        	}
				
				return false;
			}
		});
		
		//선택된 메뉴리스트로 진입한다.
		elv메뉴목록.setOnChildClickListener(new OnChildClickListener(){
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int childPosition, long id) {
				
				HashMap<String,String> data=(HashMap<String,String>)아답터.getChild(groupPosition,childPosition);
				OkjspShare.getInstance(Menu.this).getParameterDic().put("boardData",data);
				
				startActivity(new Intent(Menu.this, Board.class));
				
				return false;
			}
			
		});
		//elv메뉴목록.setDivider(null);
		//elv메뉴목록.setDividerHeight(0);
		prompt=(TextView)this.findViewById(R.id.prompt);
		
		requester=new Requester(OkjspShare.getInstance(this),this);
        requester.setAsync(new RequestAsyncHandler());
        
        //확장시켜놓는다.
        확장여부=true;
        elv메뉴목록.expandGroup(0);
        
        //db에서 shortCutMenu정보를 얻어온다.
        shortCutMenuList=new ArrayList<Map<String,String>>();
        shortCutMenu=new ShortcutMenu(this,menuList);
        shortCutMenu.getMenu(shortCutMenuList);
        
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
        //자동로그인여부에따라 자동로그인처리.
        자동로그인();
	}
	@Override
    public void onStart() {
        super.onStart();
   
        if (accelerormeterSensor != null)
            sensorManager.registerListener(this, accelerormeterSensor,
                    SensorManager.SENSOR_DELAY_GAME);
    }
   
    @Override
    public void onStop() {
        super.onStop();
   
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
    }
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		if(OkjspShare.getInstance(this).isLogin()){
			menu.add(android.view.Menu.NONE,3, 1, "즐찾추가");
			menu.add(android.view.Menu.NONE,4, 2, OkjspShare.getInstance(this).getUserId()+" 로그아웃");
			return true;
		}else{
			menu.add(android.view.Menu.NONE,5, 0, "로그인");
			return true;
		}
	}
	@Override
	public boolean onPrepareOptionsMenu (android.view.Menu menu){
			
		menu.clear();
		
		if(is즐찾추가){
			menu.add(android.view.Menu.NONE,6, 0, "즐찾추가모드 해제");
			return true;
			
		}else{
			if(OkjspShare.getInstance(this).isLogin()){
				if(isShortCutMode){
					menu.add(android.view.Menu.NONE,7, 0, "원래메뉴");
				}else{
					menu.add(android.view.Menu.NONE,2, 0, "바로가기");
					menu.add(android.view.Menu.NONE,3, 1, "즐찾추가");
					menu.add(android.view.Menu.NONE,4, 2, OkjspShare.getInstance(this).getUserId()+" 로그아웃");
				}
				return true;
			}else{
				menu.add(android.view.Menu.NONE,5, 0, "로그인");
				return true;
			}
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if(item.getItemId()==7){//원래메뉴보기
			isShortCutMode=false;
			아답터.notifyDataSetChanged();
			바로가기뷰.findViewById(R.id.헤더뷰).setVisibility(View.GONE);
		}else if(item.getItemId()==2){//바로가기..
			isShortCutMode=true;
			아답터.notifyDataSetChanged();
			바로가기뷰.findViewById(R.id.헤더뷰).setVisibility(View.VISIBLE);
			
		}else if(item.getItemId()==6){//즐찾추가모드해제
			is즐찾추가=false;
			아답터.notifyDataSetChanged();
			
		}else if(item.getItemId()==3){//즐찾추가.
			is즐찾추가=true;
			아답터.notifyDataSetChanged();
			
		}else if(item.getItemId()==4){//로그아웃..
	        
	        requester2=new Requester(OkjspShare.getInstance(this),null);
	        requester2.setAsync(new RequestAsyncHandler());
	        requester2.submitWithParameters("html5/member/logout.jsp",null);
	        
	        OkjspShare.getInstance(this).setLogin(false);
	        
	        //로그아웃 되어있다는 메세지 띄움..
	        this.prompt.setText("로그아웃 되었습니다.");
			this.prompt.setVisibility(View.VISIBLE);
			
			this.getHandler().postDelayed(new Runnable(){
				@Override
				public void run() {
					Menu.this.prompt.setText("");
					Menu.this.prompt.setVisibility(View.GONE);
				}
				
			}, 2000);
			
	        isShortCutMode=false;
	        
	        아답터.notifyDataSetChanged();
	        
		}else if(item.getItemId()==5){//로그인..
			LayoutInflater inflater=getLayoutInflater();
	    	로그인뷰=(View)inflater.inflate(R.layout.login,null);
			tv아이디=(TextView)로그인뷰.findViewById(R.id.아이디);
			tv암호=(TextView)로그인뷰.findViewById(R.id.암호);
			
			if(OkjspShare.getInstance(this).isAutoLogin()){
				tv아이디.setText(OkjspShare.getInstance(this).getUserId());
				tv암호.setText(OkjspShare.getInstance(this).getUserPassword());
			}
			tv암호.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
						imm.hideSoftInputFromWindow(tv암호.getWindowToken(), 0);
						
						return true;
					}
					return false;
				}
			});
			
			AlertDialog.Builder aDialog = new AlertDialog.Builder(this);
			aDialog.setTitle("옥희로그인");
			aDialog.setView(로그인뷰);
			aDialog.setPositiveButton("로그인", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
					imm.hideSoftInputFromWindow(tv아이디.getWindowToken(), 0);
					imm.hideSoftInputFromWindow(tv암호.getWindowToken(), 0);
					
					로그인();
				}
			});
			aDialog.setNegativeButton("취소",null);
			aDialog.show();
		}
		return (super.onOptionsItemSelected(item));
	}
	private void 자동로그인(){
		
		if(OkjspShare.getInstance(this).isAutoLogin()){
			
			this.startLoadingDialog("로그인중...");
			
			Map<String,String> parameter=new HashMap<String,String>();
			parameter.put("id",OkjspShare.getInstance(this).getUserId());
			parameter.put("password",OkjspShare.getInstance(this).getUserPassword());
			requester.submitWithParameters("html5/member/login2.jsp",parameter);
		}else{
			this.prompt.setText("로그인 하지 않았습니다.");
			this.prompt.setVisibility(View.VISIBLE);
		}
	}
	private void 로그인(){
		String 아이디=tv아이디.getText().toString().trim();
		String 암호=tv암호.getText().toString().trim();
		
		if(아이디.equals("")){
			
			tv아이디.requestFocus();
			
			Toast.makeText(this,"아이디를 입력해 주십시오.",Toast.LENGTH_SHORT).show();
			return;
		}
		if(암호.equals("")){
			
			tv암호.requestFocus();
			
			Toast.makeText(this,"암호를 입력해 주십시오.",Toast.LENGTH_SHORT).show();
			return;
		}
		
		OkjspShare.getInstance(this).setUserId(아이디);
		OkjspShare.getInstance(this).setUserPassword(암호);
		
		this.startLoadingDialog("로그인중...");
		
		Map<String,String> parameter=new HashMap<String,String>();
		parameter.put("id",아이디);
		parameter.put("password",암호);
		requester.submitWithParameters("html5/member/login2.jsp",parameter);
	}
	/**
	 * 메뉴목록 아답터..
	 * @author iiixzu
	 */
	public class 메뉴목록아답터 extends BaseExpandableListAdapter {
		
		private List<HashMap<String,String>> groupList;
		
		public 메뉴목록아답터(){
			groupList=new ArrayList<HashMap<String,String>>();
			menuList=new ArrayList<Map<String,String>>();
	        
			HashMap<String,String> menu=null;
			//그룹데이터구성..
			menu=new HashMap<String,String>();
			menu.put("groupName","OKJSP");
			menu.put("groupMemo","탭할시 게시판 목록이 나타납니다.");
			menu.put("link","");
			menu.put("imgPath","");
			groupList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("groupName","설정");
			menu.put("groupMemo","옥희에 대한 설정을 하실 수 있습니다.");
			menu.put("link","");
			menu.put("imgPath","");
			groupList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("groupName","okjsp 트윗");
			menu.put("groupMemo","@okjsp 트윗정보입니다.");
			menu.put("link","");
			menu.put("imgPath","");
			groupList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("groupName","문의 및 오류신고");
			menu.put("groupMemo","문의사항 혹은 오류발견시 트윗을 남겨 주십시오.");
			menu.put("link","");
			menu.put("imgPath","");
			groupList.add(menu);
			
			//메뉴데이터구성..
			menu=new HashMap<String,String>();
			menu.put("menuName","HTML5");
			menu.put("menuPath","html5");
			menu.put("boardId","html5");
			menu.put("imgPath","");
			menuList.add(menu);
	        
			menu=new HashMap<String,String>();
			menu.put("menuName","TECH TREND");
			menu.put("menuPath","techtrend");
			menu.put("boardId","techtrend");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","강좌");
			menu.put("menuPath","lecture");
			menu.put("boardId","lecture");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","개발툴");
			menu.put("menuPath","TOOL");
			menu.put("boardId","TOOL");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","개발툴 QnA");
			menu.put("menuPath","TOOLqna");
			menu.put("boardId","TOOLqna");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","Ajax");
			menu.put("menuPath","ajax");
			menu.put("boardId","ajax");
			menu.put("imgPath","");
			menuList.add(menu);
	        
			menu=new HashMap<String,String>();
			menu.put("menuName","Ajax QnA");
			menu.put("menuPath","ajaxqna");
			menu.put("boardId","ajaxqna");
			menu.put("imgPath","");
			menuList.add(menu);
	        
			menu=new HashMap<String,String>();
			menu.put("menuName","DB Tips");
			menu.put("menuPath","bbs2");
			menu.put("boardId","bbs2");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","DB QnA");
			menu.put("menuPath","bbs1");
			menu.put("boardId","bbs1");
			menu.put("imgPath","");
			menuList.add(menu);
	        
			menu=new HashMap<String,String>();
			menu.put("menuName","JSP Tips");
			menu.put("menuPath","bbs4");
			menu.put("boardId","bbs4");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","JSP QnA");
			menu.put("menuPath","bbs3");
			menu.put("boardId","bbs3");
			menu.put("imgPath","");
			menuList.add(menu);
	        
			menu=new HashMap<String,String>();
			menu.put("menuName","j2ee Tips");
			menu.put("menuPath","weblogic");
			menu.put("boardId","weblogic");
			menu.put("imgPath","");
			menuList.add(menu);
	        
			menu=new HashMap<String,String>();
			menu.put("menuName","j2ee QnA");
			menu.put("menuPath","weblgqna");
			menu.put("boardId","weblgqna");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","XML Tips");
			menu.put("menuPath","xmltip");
			menu.put("boardId","xmltip");
			menu.put("imgPath","");
			menuList.add(menu);
	        
			menu=new HashMap<String,String>();
			menu.put("menuName","Ruby on Rails");
			menu.put("menuPath","ruby");
			menu.put("boardId","ruby");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","Ruby on Rails QnA");
			menu.put("menuPath","rubyqna");
			menu.put("boardId","rubyqna");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","Flex");
			menu.put("menuPath","flex");
			menu.put("boardId","flex");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","Flex QnA");
			menu.put("menuPath","flexqna");
			menu.put("boardId","flexqna");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","소스자료실");
			menu.put("menuPath","bbs7");
			menu.put("boardId","bbs7");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","문서자료실");
			menu.put("menuPath","docs");
			menu.put("boardId","docs");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","기타자료실");
			menu.put("menuPath","etc");
			menu.put("boardId","etc");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","사는얘기");
			menu.put("menuPath","bbs6");
			menu.put("boardId","bbs6");
			menu.put("imgPath","");
			menuList.add(menu);

			menu=new HashMap<String,String>();
			menu.put("menuName","일본사는얘기");
			menu.put("menuPath","japanlife");
			menu.put("boardId","japanlife");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","머리식히는 곳");
			menu.put("menuPath","bbs5");
			menu.put("boardId","bbs5");
			menu.put("imgPath","");
			menuList.add(menu);
	        
			menu=new HashMap<String,String>();
			menu.put("menuName","movie story");
			menu.put("menuPath","movie");
			menu.put("boardId","movie");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","얼마면돼");
			menu.put("menuPath","howmuch");
			menu.put("boardId","howmuch");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","의견좀...");
			menu.put("menuPath","lifeqna");
			menu.put("boardId","lifeqna");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","뉴스따라잡기");
			menu.put("menuPath","news");
			menu.put("boardId","news");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","싱글의 미학");
			menu.put("menuPath","solo");
			menu.put("boardId","solo");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","구인/구직/홍보");
			menu.put("menuPath","recruit");
			menu.put("boardId","recruit");
			menu.put("imgPath","");
			menuList.add(menu);

			menu=new HashMap<String,String>();
			menu.put("menuName","english bbs");
			menu.put("menuPath","engdocs");
			menu.put("boardId","engdocs");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","번역");
			menu.put("menuPath","krtomcat");
			menu.put("boardId","krtomcat");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","추천사이트");
			menu.put("menuPath","link");
			menu.put("boardId","link");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","좋은회사");
			menu.put("menuPath","lecture");
			menu.put("boardId","lecture");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","장터");
			menu.put("menuPath","market");
			menu.put("boardId","market");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","모델2JSP책관련");
			menu.put("menuPath","model2jsp");
			menu.put("boardId","model2jsp");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","공지사항");
			menu.put("menuPath","notice");
			menu.put("boardId","notice");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","자료실문답");
			menu.put("menuPath","okboard");
			menu.put("boardId","okboard");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","유용한정보");
			menu.put("menuPath","useful");
			menu.put("boardId","useful");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","맥 정보");
			menu.put("menuPath","mac");
			menu.put("boardId","mac");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","정부는 개발자를 위해");
			menu.put("menuPath","ihaveadream");
			menu.put("boardId","ihaveadream");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","프로그램기초스터디");
			menu.put("menuPath","javastudy");
			menu.put("boardId","javastudy");
			menu.put("imgPath","");
			menuList.add(menu);
	        
			menu=new HashMap<String,String>();
			menu.put("menuName","자바패턴1기");
			menu.put("menuPath","ns");
			menu.put("boardId","ns");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","DB스터디");
			menu.put("menuPath","dbstudy");
			menu.put("boardId","dbstudy");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","스프링 스터디");
			menu.put("menuPath","springstudy");
			menu.put("boardId","springstudy");
			menu.put("imgPath","");
			menuList.add(menu);
	        
			menu=new HashMap<String,String>();
			menu.put("menuName","SLF");
			menu.put("menuPath","xf");
			menu.put("boardId","xf");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","트위터");
			menu.put("menuPath","twitter");
			menu.put("boardId","twitter");
			menu.put("imgPath","");
			menuList.add(menu);
			
			menu=new HashMap<String,String>();
			menu.put("menuName","짬통");
			menu.put("menuPath","trash");
			menu.put("boardId","trash");
			menu.put("imgPath","");
			menuList.add(menu);
	        
			menu=new HashMap<String,String>();
			menu.put("menuName","iOS");
			menu.put("menuPath","iphone");
			menu.put("boardId","iphone");
			menu.put("imgPath","");
			menuList.add(menu);
		}
        public Object getChild(int groupPosition, int childPosition) {
        	if(isShortCutMode){
        		return shortCutMenuList.get(childPosition);
        	}else{
        		return menuList.get(childPosition);
        	}
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
        	if(groupPosition==0){
        		if(확장여부){
        			if(isShortCutMode){
                		return shortCutMenuList.size();
        			}else{
        				return menuList.size();
        			}
        		}else{
        			return 0;
        		}
        	}else{
        		return 0;
        	}
        }
        
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        	
        	LinearLayout layout=null;
        	if(convertView==null){
	        	LayoutInflater inflater=getLayoutInflater();
	        	layout=(LinearLayout)inflater.inflate(R.layout.menu_item_sub,null);
        	}else{
        		layout=(LinearLayout)convertView;
        	}
        	
        	Map<String,String> map=(Map<String,String>)this.getChild(groupPosition,childPosition);
        	String 제목=map.get("menuName");
        	TextView textLabel=(TextView)layout.findViewById(R.id.textLabel);
        	textLabel.setText(제목);
        	
        	if(is즐찾추가){
        		CheckBox cb=(CheckBox)layout.findViewById(R.id.체크박스);
        		cb.setVisibility(View.VISIBLE);
        		cb.setTag(childPosition+"");
        		cb.setChecked(shortCutMenu.isCheck(childPosition));
        	}else{
        		CheckBox cb=(CheckBox)layout.findViewById(R.id.체크박스);
        		cb.setVisibility(View.GONE);
        		cb.setTag(childPosition+"");
        		cb.setChecked(shortCutMenu.isCheck(childPosition));
        	}
        	return layout;
        }

        public Object getGroup(int groupPosition) {
        	if(groupPosition>=5){
        		return null;
        	}else{
        		return groupList.get(groupPosition);
        	}
        }

        public int getGroupCount() {  
        	return groupList.size()+1;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }
        
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        	
        	LinearLayout layout=null;
        	
        	if(groupPosition==groupList.size()){ //광고
        		LayoutInflater inflater=getLayoutInflater();
        		layout=(LinearLayout)inflater.inflate(R.layout.ad,null);
        		layout.addView(AdamAdViewDelegater.getInatance(Menu.this).getAdView());
        		
        		return layout;
        	}else{
	        	if(convertView==null|| convertView.getId() != R.layout.menu_item) {
		        	LayoutInflater inflater=getLayoutInflater();
		        	layout=(LinearLayout)inflater.inflate(R.layout.menu_item,null);
	        	}else{
	        		layout=(LinearLayout)convertView;
	        	}
	        	
	        	Map<String,String> map=null;
	        	String 제목=null;
	        	String 설명=null;
	        	if(groupPosition>=5){
	        		제목="";
	            	설명="";
	        	}else{
	        		map=(Map<String,String>)this.getGroup(groupPosition);
	            	제목=map.get("groupName");
	            	설명=map.get("groupMemo");
	        	}
	        	TextView textLabel=(TextView)layout.findViewById(R.id.textLabel);
	        	TextView detailLabel=(TextView)layout.findViewById(R.id.detailLabel);
	        	textLabel.setText(제목);
	        	detailLabel.setText(설명);
	        	
	        	//textLabel.setTextColor(textLabel.getResources().getColorStateList(R.color.item_black_selector));
	        	//detailLabel.setTextColor(detailLabel.getResources().getColorStateList(R.color.item_gray_selector));
	        	
	            return layout;
        	}
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

    }
	@Override
	public void finish(Requester request, String result) {
		this.stopLoadingDialog();
		
		if(request==requester){
			int checkRange=result.indexOf("/jsp/member/logout.jsp");
			if(checkRange==-1){//로그인실패..
				this.안내창띄우기("로그인실패","로그인 하지 못하였습니다.","확인",true);
				
				this.prompt.setText("로그인 하지 않았습니다.");
				this.prompt.setVisibility(View.VISIBLE);
			}else{
				OkjspShare.getInstance(this).setLogin(true);
				
				this.prompt.setText("\""+OkjspShare.getInstance(this).getUserId()+"\"로 로그인되었습니다.");
				this.prompt.setVisibility(View.VISIBLE);
				
				this.getHandler().postDelayed(new Runnable(){
					@Override
					public void run() {
						Menu.this.prompt.setText("");
						Menu.this.prompt.setVisibility(View.GONE);
					}
					
				}, 2000);
				
				Toast.makeText(this,"바로가기메뉴로 전환하려면...\n            흔들어 보세요.",Toast.LENGTH_SHORT).show();
				
				//만일 닉넴설정이 되어있지않다면 설정하는 화면을 띄운다.
				String 닉네임=OkjspShare.getInstance(this).getNickName().trim();
				if(닉네임.equals("")){
					닉네임입력창띄우기();
				}
			}
		}
	}
	
	private TextView tv닉네임;
	private View 닉네임뷰;
	private void 닉네임입력창띄우기(){
		
		//진동주기..
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(500);
		
		//만일 닉넴설정이 되어있지않다면 설정하는 화면을 띄운다.
		LayoutInflater inflater=getLayoutInflater();
		닉네임뷰=(View)inflater.inflate(R.layout.nickname,null);
		tv닉네임=(TextView)닉네임뷰.findViewById(R.id.닉네임);
		tv닉네임.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
					imm.hideSoftInputFromWindow(tv닉네임.getWindowToken(), 0);
					
					String 닉네임=tv닉네임.getText().toString().trim();
					if(닉네임.equals("")){
						//창을 다시띄운다.
						Toast.makeText(Menu.this,"닉네임을 다시 입력해 주십시오.",Toast.LENGTH_SHORT).show();
						
						닉네임입력창띄우기();
						
					}else{
						
						OkjspShare share=OkjspShare.getInstance(Menu.this);
						share.setNickName(닉네임);
						share.commit();
					}
					return true;
				}
				return false;
			}
		});
		
		AlertDialog.Builder aDialog = new AlertDialog.Builder(this);
		aDialog.setTitle("닉네임이 설정되어 있지 않습니다.");
		aDialog.setMessage("닉네임을 입력하십시오.");
		aDialog.setView(닉네임뷰);
		aDialog.setCancelable(false);
		aDialog.setPositiveButton("입력완료", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String 닉네임=tv닉네임.getText().toString().trim();
				if(닉네임.equals("")){
					//창을 다시띄운다.
					Toast.makeText(Menu.this,"닉네임을 다시 입력해 주십시오.",Toast.LENGTH_SHORT).show();
					
					닉네임입력창띄우기();
					
				}else{
					
					OkjspShare share=OkjspShare.getInstance(Menu.this);
					share.setNickName(닉네임);
					share.commit();
				}
			}
		});
		aDialog.show();
	}
	@Override
	public void fail(Requester request) {
		this.stopLoadingDialog();
		
		this.안내창띄우기("오류","연결상태를 확인후 재시도 하십시오.","확인",true);
	}
	//즐겨찾기 체크박스를 탭했을경우 실행..
	public void 즐겨찾기추가(View view){
		CheckBox cb=(CheckBox)view;
		int index=Integer.parseInt((String)cb.getTag());
		
		if(cb.isChecked()){
			shortCutMenu.setMenuIndex(index);
			shortCutMenu.getMenu(this.shortCutMenuList);
			아답터.notifyDataSetChanged();
		}else{
			shortCutMenu.removeMenuIndex(index);
			shortCutMenu.getMenu(this.shortCutMenuList);
			아답터.notifyDataSetChanged();
		}
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
   
            if (gabOfTime > 200) {
                lastTime = currentTime;
   
                x = event.values[SensorManager.DATA_X];
                y = event.values[SensorManager.DATA_Y];
                z = event.values[SensorManager.DATA_Z];
   
                speed = Math.abs(x + y + z - lastX - lastY - lastZ)/gabOfTime * 10000;
   
                if (speed > 1600) {
                	if(OkjspShare.getInstance(this).isLogin()){ //로긴상태일경우에만 이벤트를 처리한다.
	                    // 이벤트 발생!!
                		if(is즐찾추가){ //즐찾추가모드해제.
                			is즐찾추가=false;
                			아답터.notifyDataSetChanged();
                			
                		}else if(isShortCutMode){
	                		isShortCutMode=false;
	            			아답터.notifyDataSetChanged();
	            			바로가기뷰.findViewById(R.id.헤더뷰).setVisibility(View.GONE);
	                	}else{
	                		isShortCutMode=true;
	            			아답터.notifyDataSetChanged();
	            			바로가기뷰.findViewById(R.id.헤더뷰).setVisibility(View.VISIBLE);
	                	}
                	}
                }
                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];
            }
        }
	}
}
