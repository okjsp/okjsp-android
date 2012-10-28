package kr.pe.theeye.remoteimagedownloader;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;


public class ImageDownloader
{
	public static final int IMGAE_CACHE_LIMIT_SIZE = 50;
	public static HashMap<String, Bitmap> mImageCache = new HashMap<String, Bitmap>();
	
	public static void download(String url, ImageView imageView)
	{
		Bitmap cachedImage = mImageCache.get(url);
		if(cachedImage != null)
		{
			imageView.setImageBitmap(cachedImage);
		}
		else if(cancelPotentialDownload(url, imageView))
		{
			if(mImageCache.size() > IMGAE_CACHE_LIMIT_SIZE)
			{
				mImageCache.clear();
			}
			
			ImageDownloaderTask task = new ImageDownloaderTask(url, imageView);
			DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
			imageView.setImageDrawable(downloadedDrawable);
			task.execute(url);
		}
	}

	private static boolean cancelPotentialDownload(String url, ImageView imageView)
	{
		ImageDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

		if(bitmapDownloaderTask != null)
		{
			String bitmapUrl = bitmapDownloaderTask.url;
			if((bitmapUrl == null) || (!bitmapUrl.equals(url)))
			{
				bitmapDownloaderTask.cancel(true);
			}
			else
			{
				return false;
			}
		}
		return true;
	}

	private static ImageDownloaderTask getBitmapDownloaderTask(ImageView imageView)
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

	static class DownloadedDrawable extends ColorDrawable
	{
		private final WeakReference<ImageDownloaderTask> bitmapDownloaderTaskReference;

		public DownloadedDrawable(ImageDownloaderTask bitmapDownloaderTask)
		{
			super(Color.TRANSPARENT);
			bitmapDownloaderTaskReference = new WeakReference<ImageDownloaderTask>(bitmapDownloaderTask);
		}

		public ImageDownloaderTask getBitmapDownloaderTask()
		{
			return bitmapDownloaderTaskReference.get();
		}
	}
}
