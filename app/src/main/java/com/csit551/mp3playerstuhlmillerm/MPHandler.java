package com.csit551.mp3playerstuhlmillerm;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

/*
Class to encapsulate the interaction between the activity and media player thread
 */
public class MPHandler {

    MPHandlerThread mThread;
    Handler mHandler;
    Activity mActivity;

    public MPHandler(final Activity activity) {
        mActivity = activity;

        // Find out if we already have a running handler thread (orientation switch).
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        Thread[] threads = new Thread[rootGroup.activeCount()];
        rootGroup.enumerate(threads, false);
        for (int ind = 0; ind < threads.length; ind++) {
            Thread t = threads[ind];
            if (t.isAlive() && t.getName() == "MPHandlerThread") {
                // Set our thread from the one running and re-initialize the UI.
                mThread = (MPHandlerThread) t;
                mThread.attachParent(this, activity);
                handlerReady();
                updateStatus();
                updateBitmap();
            }
        }

        if (mThread == null) {
            // We didn't find a thread running, so create a new one
            mThread = new MPHandlerThread(this, activity, R.raw.la_traviata);
            mThread.start();
        }
    }

    // Called when the handler for the thread has been created (or reattached)
    public void handlerReady() {
        mHandler = mThread.getHandler();
    }

    // Send a command to the handler thread (e.g. start / stop / pause)
    public void command (String cmd) {

        // Silently ignore the command if the player thread isn't ready yet
        if (mHandler == null) {
            return;
        }

        Message msg = Message.obtain();
        msg.what = 1;
        msg.obj = cmd;

        mHandler.sendMessage(msg);
    }

    // Send a message to the handler thread with the URL to stream (disabled for now)
    public void stream (String url) {
        if (mHandler != null) {
            Toast.makeText(mActivity.getApplicationContext(), "Streaming coming soon... maybe...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mHandler == null) {
            Toast.makeText(mActivity.getApplicationContext(), "Player not ready. Try again in a moment.", Toast.LENGTH_SHORT).show();
            return;
        }

        Message msg = Message.obtain();
        msg.what = 2;
        msg.obj = url;

        mHandler.sendMessage(msg);
    }

    // Get the bitmap extracted from the MP3 to update the image view on the UI thread
    public void updateBitmap() {
        final Bitmap bitmap = mThread.getBitmap();

        if (bitmap != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    ImageView img = mActivity.findViewById(R.id.coverImage);
                    img.setImageBitmap(bitmap);
                }
            });
        }
    }

    // Get the status from the handler thread and use it to update the UI text view
    public void updateStatus() {
        final String status = mThread.getStatus();

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                TextView txt = mActivity.findViewById(R.id.statusView);
                txt.setText(status);
            }
        });

    }

}
