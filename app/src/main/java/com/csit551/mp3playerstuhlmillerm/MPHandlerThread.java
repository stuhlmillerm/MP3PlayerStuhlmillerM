package com.csit551.mp3playerstuhlmillerm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.ThreadFactory;

/*
Class to extend standard HandlerThread to utilize the MediaPlayer object.
 */
public class MPHandlerThread extends HandlerThread {
    private static final String TAG = "MPHandlerThread";
    Activity mActivity;
    final int mResource;
    MPHandler mParent;
    Bitmap mBitmap;
    String mStatus;

    private Handler handler;
    MediaPlayer player;

    // Use the constructor to link back to our parent (MPHandler) object and activity
    public MPHandlerThread(MPHandler parent, Activity activity, int resourceId) {
        super(TAG);

        mActivity = activity;
        mResource = resourceId;
        mParent = parent;
    }

    // For when we are running already and a new activity and parent are created
    public void attachParent(MPHandler parent, Activity activity) {
        mActivity = activity;
        mParent = parent;
    }

    // Create the MediaPlayer with the media, either the built-in or stream
    public void setupPlayer(String url) {
        if (url == null || url == "") {
            player = MediaPlayer.create(mActivity, R.raw.la_traviata);
        } else {
            player = MediaPlayer.create(mActivity, Uri.parse(url));
        }
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        // When the thread is first prepared, set up the player for the built in MP3
        setupPlayer(null);
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared(MediaPlayer mp)
            {
                // Set our status
                setStatusText("Player ready.  Press play.");

                /*
                See if we can find cover art for the MP3
                Adapted from https://stackoverflow.com/questions/10209176/extract-album-cover-from-mp3-file-in-android
                Replaces default android guy with headphones (adapted for icon too) courtesy of
                https://www.androidguys.com/tips-tools/how-to-set-your-favorite-song-as-alarm-on-your-android-phone/
                 */
                android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                AssetFileDescriptor afd=mActivity.getResources().openRawResourceFd(mResource);
                mmr.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                byte [] data = mmr.getEmbeddedPicture();

                // convert the byte array to a bitmap if we found it
                if(data != null)
                {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    mBitmap = bitmap;
                    mParent.updateBitmap();
                }

            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    // Figure out which command it is and update our player and status
                    switch ((String) msg.obj) {
                        case "start":
                            player.start();
                            setStatusText("Playing MP3.");
                            break;
                        case "pause":
                            player.pause();
                            setStatusText("Playback paused.");
                            break;
                        case "resume":
                            player.start();
                            setStatusText("Playback resumed.");
                            break;
                        case "stop":
                            player.stop();
                            setStatusText("Playback stopped. Click restart to play again.");
                            break;
                        case "repeat":
                            setStatusText("Restarting player.");
                            player.reset();
                            setupPlayer(null);
                            player.start();
                            setStatusText("Playback restarted.");
                            break;
                    }
                } else if (msg.what == 2) {
                    // Stream URL passed in - reset the player and set it up with the URL
                    setStatusText("Loading stream from URL provided.");
                    if (player != null)
                        player.reset();
                    setupPlayer((String) msg.obj);
                }
            }
        };

        // Tell our parent (MPHandler) that the thread handler is ready.
        mParent.handlerReady();

    }

    // Update the thread's status and tell the parent to update the UI
    public void setStatusText(String status) {
        mStatus = status;
        mParent.updateStatus();
    }

    // Return the current status for the parent to update the UI
    public String getStatus() {
        return mStatus;
    }

    // Return the parsed bitmap for the parent to update the UI
    public Bitmap getBitmap() {
        return mBitmap;
    }

    // Return the handler for the parent to use for message sending
    public Handler getHandler() {
        return handler;
    }

}
