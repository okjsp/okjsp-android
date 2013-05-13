package net.okjsp.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.okjsp.Const;
import android.net.Uri;

public class Board implements Externalizable, Const {
	protected String Title;
	protected Uri    NameUri; // 'board://notice'
	protected int ClickCount;
	protected long TimeStamp;
	
	public Board(String title, String uri) {
		setTitle(title);
		setUri(uri);
	}
	
	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	public String getId() {
		return NameUri.getHost();
	}
	public String getUrl() {
		return BBS_BOARD_URL + NameUri.getHost();
	}
	public Uri getUri() {
		return NameUri;
	}
	public void setUri(String uri) {
		NameUri = Uri.parse(uri);
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
		setUri(in.readUTF());
		setClickCount(in.readInt());
		setTimeStamp(in.readLong());
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(Title);
		out.writeUTF(getUri().toString());
		out.writeInt(ClickCount);
		out.writeLong(getTimeStamp());
	}
}
