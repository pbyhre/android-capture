package com.openbiometrix.capture.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.google.android.gms.vision.face.Face;
import com.openbiometrix.capture.CaptureDevice;
import com.openbiometrix.capture.FaceDetectorBuffer;

/**
 * View used to display the stream from a camera used in a FaceCaptureDevice.  The view also
 * contains a FaceDetectorBuffer which receives Face detection information from the FaceCaptureDevice
 * for the most prominent face in the image.
 *
 * Created by petebyhre on 4/10/17.
 */
public class FaceCaptureView extends SurfaceView implements CaptureView
{
	private final static String TAG = "FaceCaptureView";


	/**
	 * Constructor that starts the face detection loop.
	 *
	 * @param ctx
	 */
	public FaceCaptureView(Context ctx)
	{
		super(ctx);
		startFaceDetectionLoop();
	}

	/**
	 * Constructor that starts the face detection loop.
	 *
	 * @param context
	 * @param attrs
	 */
	public FaceCaptureView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		startFaceDetectionLoop();
	}

	/**
	 * Constructor that starts the face detection loop.
	 *
	 * @param context
	 * @param attrs
	 * @param defStyleAttr
	 */
	public FaceCaptureView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		startFaceDetectionLoop();
	}


	/**
	 * A View that is used to display the capture stream and / or other data related to the capture.
	 */
	@Override
	public View getView()
	{
		return this;
	}

	/**
	 * Returns an implementation of a capture buffer.  This is a bounded queue that is the
	 * pipeline for transmitted from the CaptureDevice.
	 *
	 * @return A FaceDetectorBuffer that will handle a stream of Face locator objects
	 */
	@Override
	public FaceDetectorBuffer getCaptureBuffer()
	{
		return mFaceDetectorBuffer;
	}

	/**
	 * State change notifier that alerts the CaptureView that there is a change in the state
	 * of the CaptureDevice
	 *
	 * @param state
	 */
	@Override
	public void onStateChanged(CaptureDevice.State state)
	{
		Log.d(TAG, "onStateChanged() " + state);
		switch (state)
		{
			case PREVIEW_STARTED:
				mFaceUpdateCount = 0;
				mStartTime = System.currentTimeMillis();
				break;

			case PAUSED:
			case STOPPED:
				stopFaceDetectionLoop();
				break;
		}
	}

	/**
	 * Update from the FaceDetectionLoop.  For now, it just updates a count of the
	 * Face location data objects received.
	 *
	 * @param face
	 */
	public void onUpdate(Face face)
	{
		mFaceUpdateCount++;
	}

	private void stopFaceDetectionLoop()
	{
		Log.d(TAG, "Stopping Face Detector Loop");
		mStopFaceDetectorLoop = true;
		if (mFaceDetectorThread != null)
		{
			mFaceDetectorThread.interrupt();
		}
	}

	/**
	 * Background thread that pulls Face locator objects out of the buffer from the
	 * FaceCaptureDevice.
	 */
	private void startFaceDetectionLoop()
	{
		// if we are already running the thread, then stop it.
		if (mFaceDetectorThread != null)
		{
			mStopFaceDetectorLoop = false;
			try
			{
				mFaceDetectorThread.join();
			}
			catch (InterruptedException ex)
			{
				return;
			}
		}

		// start the face detector thread loop.
		mStopFaceDetectorLoop = false;
		mFaceDetectorThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (mStopFaceDetectorLoop == false)
				{
					try
					{
						final Face face = mFaceDetectorBuffer.take();

						if (face != null)
						{
							// TODO: Analyze face values and recognize when a face is available.  For now just send face to ui thread.
							mSelf.post(new Runnable()
							{
								@Override
								public void run()
								{
									onUpdate(face);
								}
							});
						}

					}
					catch (InterruptedException ex)
					{
						// the take() call on the face detect buffer will block.
						// if the thread interrupts, we need to check to see if we
						// are supposed to stop face detection.
					}
				}
				mEndTime = System.currentTimeMillis();
				Log.d(TAG, "Face Update Count=" + mFaceUpdateCount);
				Log.d(TAG, "Start Time=" + mStartTime + " End Time=" + mEndTime);
				Log.d(TAG, "Face detection updates per second=" + (double) mFaceUpdateCount / (double)(mEndTime - mStartTime) * 1000);
			}
		});
		mFaceDetectorThread.start();

	}


	private FaceDetectorBuffer 	mFaceDetectorBuffer = new FaceDetectorBuffer();
	private boolean 			mStopFaceDetectorLoop = false;
	private View 				mSelf = this;
	private int 				mFaceUpdateCount = 0;
	private long 				mStartTime = 0;
	private long 				mEndTime = 0;
	private Thread 				mFaceDetectorThread = null;
}
