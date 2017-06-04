package com.openbiometrix.capture;

/**
 * Created by petebyhre on 4/10/17.
 */

import android.content.Context;
import android.util.Log;

import com.openbiometrix.capture.exception.DeviceStateException;
import com.openbiometrix.capture.exception.InvalidDeviceBuffer;
import com.openbiometrix.capture.ui.CaptureView;

import java.util.Vector;


/**
 * An abstract class that describes, and provides a common interface to control, a biometric
 * capture device.  The information that a capture device creates is dependent upon the type
 * of capture device.  For example, a face capture device presents data about a face or faces
 * that are present in a given frame, while an audio voice capture will contain a stream of
 * wav audio data.
 *
 * The specific capture device implementation streams this data through to any capture viewers
 * that are attached to it through a blocking queue.
 */
public abstract class CaptureDevice
{
	protected final String TAG = getId();

	/**
	 * The possible states that a capture device can be in.
	 */
	public enum State
	{
		UNINITIALIZED,
		INITIALIZED,
		STOPPED,
		PREVIEW_STARTED,
		CAPTURE_STARTED,
		PAUSED

	}

	/**
	 * The possible types of biometric capture devices.
	 */
	public enum Type
	{
		UNKNOWN,
		CAMERA,
		MICROPHONE,
		FINGERPRINT,
		VIDEO
	}


	/**
	 * A string identifier that uniquely identifies this capture device.
	 */
	public abstract String getId();

	/**
	 * A formatted name for the capture device.
	 */
	public abstract String getName();

	/**
	 * Version number for the capure device.
	 */
	public abstract String getVersion();

	/**
	 * Vendor that created the capture device.
	 */
	public abstract String getVendor();

	/**
	 * Type of the capture device.  Possible values are: CAMERA, MICROPHONE, FINGERPRINT, VIDEO
	 */
	public abstract Type getType();


	/**
	 * Add a CaptureView that will present the data captured by the device.  All viewers receive
	 * the same capture data.
	 *
	 * @param viewer
	 * @throws InvalidDeviceBuffer
	 */
	public abstract void addViewer(CaptureView viewer)  throws InvalidDeviceBuffer;

	/**
	 * Remove a viewer from the list of active CaptureView objects.
	 *
	 * @param viewer
	 */
	public synchronized void removeViewer(CaptureView viewer)
	{
		mViewerList.remove(viewer);
	}

	/**
	 * Initializes a capture device.  This will put the device in a state ready to begin capture.
	 *
	 * @param ctx Android Application Context
	 * @return true if successful, else false.
	 */
	public abstract boolean initialize(Context ctx);

	/**
	 * Initializes a capture device.  This will put the device in a state ready to begin capture.
	 *
	 * @param ctx Android Application Context
	 * @param config JSON string that defines the configuration to be used to initialize the device.
	 * @return true if successful, else false.
	 */
	public abstract boolean initialize(Context ctx, String config);

	/**
	 * Uninitializes the capture device and releases any held resources.
	 */
	public abstract void destroy();

	/**
	 * Starts the preview of a device.  This can be useful for devices that provide feedback prior
	 * to actually capturing data.
	 */
	public abstract void startPreview();

	/**
	 * Begins the actual capturing of data for the device.
	 */
	public abstract void startCapture();

	/**
	 * Will pause previewing from the capture device.  If the device is also capturing at the time,
	 * capture will be paused as well.
	 *
	 * @throws DeviceStateException
	 */
	public void pause() throws DeviceStateException
	{
		switch (mState)
		{
			case PREVIEW_STARTED:
			case CAPTURE_STARTED:
				mPausedState = mState;
				setState(State.PAUSED);
				break;

			case STOPPED:
				throw new DeviceStateException("error.device.already.stopped");

			case PAUSED:
				// already paused.  do nothing
				break;
		}
	}

	/**
	 * Will resume preview and / or capture from the device if it has been paused.
	 *
	 * @throws DeviceStateException If the device is stopped, it cannot be resumed.
	 */
	public void resume() throws DeviceStateException
	{
		switch (mState)
		{
			case PREVIEW_STARTED:
			case CAPTURE_STARTED:
				// do nothing
				break;

			case STOPPED:
				throw new DeviceStateException("error.device.already.stopped");

			case PAUSED:
				setState(mPausedState);
				break;
		}

	}

	/**
	 * Stop preview and capture from the device.
	 */
	public void stop()
	{
		setState(State.STOPPED);
	}

	/**
	 * Some devices store the captured content.  If so, then this function tells how many files
	 * were saved by the device.
	 *
	 * @return Number of files saved by the device.
	 */
	public int getCapturedFileCount()
	{
		return mCapturedFiles == null ? 0 : mCapturedFiles.size();
	}

	/**
	 * If the capture device saves captured files, then this provides information about those files.
	 *
	 * @return A list of files captured by the device.
	 */
	public Vector<CaptureFile> getCapturedFiles()
	{
		return mCapturedFiles;
	}

	/**
	 * Set the current state of the capture device.
	 *
	 * @param state
	 */
	protected void setState(State state)
	{
		mState = state;

		for (CaptureView viewer : mViewerList)
		{
			viewer.onStateChanged(state);
		}
		Log.d(TAG, "setState: " + mState);
	}

	/**
	 * Get the current state of the capture device.
	 */
	public State getState()
	{
		return mState;
	}


	protected Vector<CaptureView> 	mViewerList = new Vector<CaptureView>();
	protected Vector<CaptureFile> 	mCapturedFiles = null;
	private State 					mState = State.UNINITIALIZED;
	private State 					mPausedState = State.UNINITIALIZED;

}