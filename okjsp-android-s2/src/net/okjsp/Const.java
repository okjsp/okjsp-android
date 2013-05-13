package net.okjsp;

public interface Const {
	public static final String BASE_URL = "http://okjsp.net";
	public static final String MAIN_BOARD = "/bbs?act=FIRST_MAIN";
	public static final String BBS_BOARD = "/bbs?act=LIST&bbs=";
	public static final String MAIN_BOARD_URL = BASE_URL + MAIN_BOARD;
	public static final String BBS_BOARD_URL = BASE_URL + BBS_BOARD;
	
	public static final String BOARD_URI_SCHEME = "board://";
	public static final String IMAGE_CACHE_DIR = "cache";
	
	public static final int POST_TYPE_NOTICE = 1;
    public static final int POST_TYPE_RECENT = 2;
	
	public static final int MSG_PARSE_MAIN_PAGE_DONE = 1;
	public static final int MSG_PARSE_BOARD_PAGE_DONE = 2;
}
