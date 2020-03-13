package net.kultprosvet.androidcourse.socialapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.util.HashMap;

public class ThumbnailExtract extends AsyncTask<String, long[], Bitmap> {
    private String videoUrl;
    private ImageView mThumbnail;
    private boolean mIsVideo;
    private MediaMetadataRetriever mmr;

    public ThumbnailExtract(String videoLocalUrl, ImageView thumbnail, boolean isVideo) {
        this.videoUrl = videoLocalUrl;
        mThumbnail = thumbnail;
        mIsVideo = isVideo;
        if (!isVideo) {
            mmr = new MediaMetadataRetriever();
        }
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        if (!mIsVideo) {
            return getBitmap(videoUrl);
        } else {
            return retrieveVideoFrameFromVideo(videoUrl);
        }
    }

    private static Bitmap retrieveVideoFrameFromVideo(String videoPath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());

            Bitmap bm = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST);
            bitmap = Bitmap.createScaledBitmap(bm, 50, 50, false);
        } catch (Throwable t) {
            t.printStackTrace();
            //"Exception in retrieveVideoFrameFromVideo(String videoPath)"
        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap thumb) {
        if (thumb != null) {
            mThumbnail.setImageBitmap(thumb);
        }
    }

    private Bitmap getBitmap(String fileUrl) {
        mmr.setDataSource(fileUrl);
        byte[] data = mmr.getEmbeddedPicture();
        Bitmap bitmap = null;
        // convert the byte array to a bitmap
        if (data != null) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        }
        return bitmap != null ? Bitmap.createScaledBitmap(bitmap, 50, 50, false) : null;
    }
}