package kr.pe.theeye.remoteimagedownloader;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import kr.pe.theeye.remoteimagedownloader.ImageDownloader.DownloadedDrawable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap>
{
	public String url;
	public String targetUrl;
	private WeakReference<ImageView> imageViewReference;

	public ImageDownloaderTask(String url, ImageView imageView)
	{
		this.targetUrl = url;
		this.imageViewReference = new WeakReference<ImageView>(imageView);
	}

	@Override
	protected Bitmap doInBackground(String... params)
	{
		return downloadBitmap(params[0]);
	}

	@Override
	protected void onPostExecute(Bitmap bitmap)
	{
		if(isCancelled())
		{
			bitmap = null;
		}

		if(imageViewReference != null)
		{
			ImageView imageView = imageViewReference.get();
			ImageDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
			
			if(this == bitmapDownloaderTask)
			{
				ImageDownloader.mImageCache.put(targetUrl, bitmap);
				imageView.setImageBitmap(bitmap);
			}
		}
	}
	
	private ImageDownloaderTask getBitmapDownloaderTask(ImageView imageView)
	{
		if(imageView != null)
		{
			Drawable drawable = imageView.getDrawable();
			if(drawable instanceof DownloadedDrawable)
			{
				DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	static Bitmap downloadBitmap(String url)
	{
		final HttpClient client = new DefaultHttpClient();
		final HttpGet getRequest = new HttpGet(url);

		try
		{
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode != HttpStatus.SC_OK)
			{
				Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if(entity != null)
			{
				InputStream inputStream = null;
				//BitmapFactory.Options options = new BitmapFactory.Options();
				//options.inSampleSize = 2;
				
				try
				{
					inputStream = entity.getContent();
					final Bitmap bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
					//final Bitmap bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream), null, options);
					return bitmap;
				}
				finally
				{
					if(inputStream != null)
					{
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		}
		catch(Exception e)
		{
			getRequest.abort();
		}
		return null;
	}

	static class FlushedInputStream extends FilterInputStream
	{
		public FlushedInputStream(InputStream inputStream)
		{
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException
		{
			long totalBytesSkipped = 0L;
			while(totalBytesSkipped < n)
			{
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if(bytesSkipped == 0L)
				{
					int bytes = read();
					if(bytes < 0)
					{
						break; // we reached EOF
					}
					else
					{
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}
}
