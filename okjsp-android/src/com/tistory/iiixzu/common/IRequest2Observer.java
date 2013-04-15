package com.tistory.iiixzu.common;
/**
 * Requester객체가 Request작업이 끝났을 경우에 옵져버객체가 등록되어있으면
 * 해당 옵져버객체에 메세지를 보낸다. 이때 쓰이는 인터페이스.
 * @author iiixzu
 */
public interface IRequest2Observer {
	/**
	 * 요청이 성공했을때 호출됨.
	 * @param request 어떤 Request객체인지.
	 * @param result 결과값.
	 */
	public void finish(RequesterForKeepLogined request,String result);
	/**
	 * 요청이 실패했을때 호출됨.
	 * @param request 어떤 Request객체인지.
	 */
	public void fail(RequesterForKeepLogined request);
}
