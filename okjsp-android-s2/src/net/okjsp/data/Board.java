package net.okjsp.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import android.net.Uri;
import android.text.TextUtils;

public class Board implements Externalizable {
	protected String Title;
	protected String Url;
	protected Uri    mUri;
	protected int Index;
	protected int ClickCount;
	protected long TimeStamp;
	
	public Board(String title, String url) {
		setTitle(title);
		setUrl(url);
	}
	
	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	public String getId() {
		return mUri.getHost();
	}
	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
		if (!TextUtils.isEmpty(url)) {
			mUri = Uri.parse(url);
		}
	}
	public Uri getUri() {
		return mUri;
	}
	public int getIndex() {
		return Index;
	}
	public void setIndex(int index) {
		Index = index;
	}
	public int getClickCount() {
		return ClickCount;
	}
	public void setClickCount(int clickCount) {
		ClickCount = clickCount;
	}
	public long getTimeStamp() {
		return TimeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		TimeStamp = timeStamp;
	}
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		setTitle(in.readUTF());
		setUrl(in.readUTF());
		setIndex(in.readInt());
		setClickCount(in.readInt());
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(Title);
		out.writeUTF(Url);
		out.writeInt(Index);
		out.writeInt(ClickCount);
	}
}
