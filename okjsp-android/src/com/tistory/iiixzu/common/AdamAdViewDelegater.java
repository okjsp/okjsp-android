package com.tistory.iiixzu.common;

import net.daum.adam.publisher.AdView;
import net.daum.adam.publisher.AdView.AnimationType;
import net.daum.adam.publisher.AdView.OnAdFailedListener;
import net.daum.adam.publisher.AdView.OnAdLoadedListener;
import net.daum.adam.publisher.impl.AdError;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

/**
 * 광고호출을 대행하는 공통 클래스이다.
 * @author iiixzu
 *
 */
public class AdamAdViewDelegater {
	
	private static AdamAdViewDelegater delegater;
	
	private AdView adView;
	private LinearLayout.LayoutParams params;
	private boolean is광고있니;
	
	private Context context;
	private AdamAdViewDelegater(Context context){
		this.context=context;
	}
	public static AdamAdViewDelegater getInatance(Context context){
		
		if(delegater==null){
			delegater=new AdamAdViewDelegater(context);
		}
		
		return delegater;
	}
	public AdView getAdView(){
		
		if(adView!=null){
			adView.destroy();
		}
		
		initAdam();
		
		return adView;
	}
	public void initAdam() {
		// Ad@m 광고 뷰 생성 및 설정
		adView = new AdView(context);
		params=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,70);
		adView.setLayoutParams(params);
		
		// 광고 내려받기 실패했을 경우에 실행할 리스너
		adView.setOnAdFailedListener(new OnAdFailedListener() {
			@Override
			public void OnAdFailed(AdError arg0, String arg1) {
				
				is광고있니=false;
			}
		});

		// 광고를 정상적으로 내려받았을 경우에 실행할 리스너
		adView.setOnAdLoadedListener(new OnAdLoadedListener() {

			@Override
			public void OnAdLoaded() {
				adView.setLayoutParams(params);
				is광고있니=true;
			}
		});

		// 할당 받은 clientId 설정
		adView.setClientId("XXXXXXXXXXXXX"); //오픈소스배포를 위해 이부분삭제.
		// 광고 갱신 시간 : 기본 60초
		adView.setRequestInterval(60);
		// Animation 효과 : 기본 값은 AnimationType.NONE
		adView.setAnimationType(AnimationType.FLIP_HORIZONTAL);
		adView.setVisibility(View.VISIBLE);
	}
	
	public boolean hasAd(){
		return is광고있니;
	}
}
