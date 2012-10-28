/*
 *@(#)StringHelper.java 
 *Copyright (C) 2005 Lee,Gyujoo - MAILTO:iiixzu@gmail.com
 *
 *이 프로그램은 자유 소프트웨어입니다; 소프트웨어의 피양도자는 자유 소프트웨어 재단이 공표한 GNU 일반 
 *공중 사용 허가서 2판 또는 그 이후 판을 임의로 선택해서, 그 규정에 따라 프로그램을 개작하거나  재배포할 
 *수 있습니다. 
 *
 *이 프로그램은 유용하게 사용될 수 있으리라는 희망에서 배포되고 있지만, 특정한 목적에 맞는 적합성  여부
 *나 판매용으로 사용할 수 있으리라는 묵시적인 보증을 포함한 어떠한 형태의 보증도 제공하지 않습니다;  보
 *다 자세한 사항에 대해서는 GNU 일반 공중 사용 허가서를 참고하시기 바랍니다. 
 *
 *GNU 일반 공중 사용 허가서는 이 프로그램과 함께 제공됩니다; 만약, 이 문서가 누락되어 있다면  자유 소프
 *트웨어 재단으로 문의하시기 바랍니다. (자유 소프트웨어 재단:  Free Software Foundation, Inc., 59 Temple 
 *Place - Suite 330, Boston, MA 02111-1307, USA)
 */
package com.tistory.iiixzu.common;

import java.util.Vector;

/**
 * 문자열 관련한 유틸성 기능을 제공하는 클래스.
 * @author 이규주 (<a href="mailto:iiixzu@gamil.com">iiixzu@gmail.com</a>)
 */
public class StringHelper {
	/**
	 *	허용되지 않는 문자검사.
	 *	허용되지 않는 문자를 검사하여 이를 뺀 값만 반환한다.
	 *  예) 주어진 문장중 캐리지 리턴값 \r\n을 검사하여 빼버리려할때 인자로서 패턴으로\r\n을준다면
	 *  해당 값이 빼어진 문장이 반환되게된다.
	 * @param msg 검사하려는 문자열
	 * @param pattern 검사하려는 패턴
	 * @return String 검사완료된 무결한 문자열
	 */
	public static String filt(String msg,String pattern){
		String temp = "";
		int patternLength=pattern.length();
		
		while(msg.indexOf(pattern)!=-1){
			temp += msg.substring(0, msg.indexOf(pattern));
			msg = msg.substring(msg.indexOf(pattern)+patternLength, msg.length()) ;
			if(msg.indexOf(pattern)==-1) {
				temp += msg;
			}
		}
		if(temp.equals("")){
			return msg;
		}else{
			return temp;
		}
	}
	/**
	 * 문자를 원하는 다른 문자로 감싼다.
	 * <p><pre>
	 * lapStr("하나","'")-> '하나'
	 * </pre></p>
	 * @param x 감싸려는문장
	 * @return 결과값
	 */
	public static String lap(String x,String lap){
	    return lap+x+lap;
	}
	/**
	 * 문자열분리.주어진 문자(열)로 나누어진 각각의 문자열을 배열로서 반환한다.
	 * 자바에서 spilt와 같은 유사한 기능이 제공되나 | 과 같은형태는 분리해내지 못한다.
	 * 이 메소드에서는 그와같은 분리안되는 문자에 대해서도 분리가 되게끔 구현되었다.
	 * 
	 * <p><pre>
	 *  예1) 우리나라||좋은나라||세상에||이런일이
	 *         String[0] : 우리나라 
	 *         String[1] : 좋은나라 
	 *         String[2] : 세상에 
	 *         String[3] : 이런일이 
	 *  예2)  ||우리나라||좋은나라||세상에||이런일이||
	 *         String[0] : ""(빈문자열)
	 *         String[1] : 우리나라 
	 *         String[2] : 좋은나라 
	 *         String[3] : 세상에 
	 *         String[4] : 이런일이
	 *         String[5] : ""(빈문자열)
	 *  예3) 우리나라||좋은나라||||세상에||이런일이
	 *         String[0] : 우리나라
	 *         String[1] : 좋은나라
	 *         String[2] : ""(빈문자열)
	 *         String[3] : 세상에
	 *         String[4] : 이런일이
	 * </pre></p>
	 * @param msg 파싱하려는 문자열
	 * @param delim 문자열구분을 위해 쓰이는 문자열
	 * @return String[] 파싱된 문자배열.
	 */
	public static String[] tokenize(String msg,String delim){
		
		String[] returnString=null;		
		Vector list=new Vector();
		
		int patternLength=delim.length();
		
		while(msg.indexOf(delim)!=-1){
			list.addElement(msg.substring(0,msg.indexOf(delim)));
			msg = msg.substring(msg.indexOf(delim)+patternLength, msg.length()) ;
			if(msg.indexOf(delim)==-1) {
				list.addElement(msg);
			}
		}
		if(list.size()==0){
			returnString = new String[1];
			returnString[0] = new String(msg);			
			return returnString;
		}else{
			returnString = new String[list.size()];
			for(int i=0;i<list.size();i++){
				//널일경우에는 ""빈문자열을 넘겨준다.
				returnString[i]=list.elementAt(i)==null?"":(String)list.elementAt(i);
			}
			return returnString;
		}
	}
}
