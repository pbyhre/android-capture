package com.openbiometrix.capture;

/**
 * Created by petebyhre on 4/10/17.
 */

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;
import com.openbiometrix.capture.exception.DeviceStateException;
import com.openbiometrix.capture.exception.InvalidDeviceBuffer;
import com.openbiometrix.capture.exception.NoDeviceFoundException;
import com.openbiometrix.capture.ui.CaptureView;

import java.io.IOException;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
import static com.openbiometrix.capture.CaptureDevice.Type.CAMERA;


/**
 * Face camera used for detecting and capturing faces captured by a CameraSource.
 * This class uses the Google Vision API for face detection.
 */
public class FaceCaptureDevice extends CaptureDevice implements CameraSource.PictureCallback
{
	/**
	 * A string identifier that uniquely identifies this capture device.
	 */
	@Override
	public String getId()
	{
		return "com.openbiometrix.capture.FaceCaptureDevice";
	}

	/**
	 * A formatted name for the capture device.
	 */
	@Override
	public final String getName()
	{
		return "Face Capture Device";
	}

	/**
	 * Version number for the capure device.
	 */
	@Override
	public String getVersion()
	{
		return "1.0";
	}

	/**
	 * Vendor that created the capture device.
	 */
	@Override
	public String getVendor()
	{
		return "OpenBiometrix";
	}

	/**
	 * Type of the capture device.
	 */
	@Override
	public Type getType()
	{
		return CAMERA;
	}

	/**
	 * Initialize the front facing camera with default characteristics and attach a face detector
	 * that will only pick out the largest face in the frame.
	 *
	 * @param ctx Android Application Context
	 * @return
	 */
	@Override
	public boolean initialize(Context ctx)
	{
		return initialize(ctx, null);
	}

	/**
	 * Initialize a camera with default characteristics and attach a face detector that will only
	 * pick out the largest face in the frame.
	 *
	 * @param ctx Android Application Context
	 * @param config JSON string that defines the configuration to be used to initialize the device.
	 * @return
	 */
	@Override
	public boolean initialize(Context ctx, String config)
	{
		try
		{
			//TODO: add in json parsing of config string to get capture parameters

			FaceDetector detector = new FaceDetector.Builder(ctx)
					.setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
					.setProminentFaceOnly(true)
					.build();

			detector.setProcessor(new LargestFaceFocusingProcessor.Builder(detector, new FaceTracker())
					.build());

			if (!detector.isOperational())
			{
				// Note: The first time that an app using face API is installed on a device, GMS will
				// download a native library to the device in order to do detection.  Usually this
				// completes before the app is run for the first time.  But if that download has not yet
				// completed, then the above call will not detect any faces.
				//
				// isOperational() can be used to check if the required native library is currently
				// available.  The detector will automatically become operational once the library
				// download completes on device.
				Log.w(TAG, "Face detector dependencies are not yet available.");
			}

			m_cameraSource = new CameraSource.Builder(ctx, detector)
					.setRequestedPreviewSize(640, 480)
					.setFacing(getCameraSource(ctx))
					.setRequestedFps(30.0f)
					.build();

			return true;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	/**
	 * Use the front facing camera if available.  If there is no front camera, choose the last
	 * available internal camera.
	 *
	 * @return
	 */
	private int getCameraSource(Context ctx) throws CameraAccessException
	{
		int bestCamera = -1;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			CameraManager manager = (CameraManager) ctx.getSystemService(Context.CAMERA_SERVICE);

			for (String camera : manager.getCameraIdList())
			{
				try
				{
					int curCamera = Integer.parseInt(camera);
					// 0 is rear facing camera, 1 is front.
					if (curCamera == 1)
					{
						return curCamera;
					}
					bestCamera = curCamera;
				}
				catch (NumberFormatException ex)
				{
					// do nothing.  this is an external camera.
				}
			}
			return bestCamera;
		}
		else
		{
			int numCameras= Camera.getNumberOfCameras();
			for(int i=0;i<numCameras;i++)
			{
				Camera.CameraInfo info = new Camera.CameraInfo();
				Camera.getCameraInfo(i, info);
				if(CAMERA_FACING_FRONT == info.facing)
				{
					return CameraSource.CAMERA_FACING_FRONT;
				}
				bestCamera = info.facing;
			}
		}
		// no camera found;
		if (bestCamera == -1)
		{
			throw new NoDeviceFoundException();
		}
		return bestCamera;
	}

	/**
	 * Uninitializes the capture device and releases any held resources.
	 */
	@Override
	public void destroy()
	{
		if (m_cameraSource != null)
		{
			m_cameraSource.release();
		}
	}

	/**
	 * Starts the preview of a device.  This can be useful for devices that provide feedback prior
	 * to actually capturing data.
	 */
	@Override
	public void startPreview()
	{
		if (m_cameraSource != null)
		{
			try
			{
				// find the SurfaceView in the collection of CaptureView
				// there can only be one.
				for (CaptureView viewer : mViewerList)
				{
					View v = viewer.getView();
					if (v instanceof SurfaceView)
					{
						m_cameraSource.start(((SurfaceView)v).getHolder());
						setState(State.PREVIEW_STARTED);
						break;
					}
				}

			}
			catch (IOException | SecurityException e)
			{
				Log.e(TAG, "Unable to start camera source.", e);
				m_cameraSource.release();
				m_cameraSource = null;
			}
		}
	}

	/**
	 * startCapture is a one-shot image capture
 	 */
	@Override
	public void startCapture()
	{
		m_cameraSource.takePicture(null, this);
	}

	/**
	 *
	 * @param picture
	 */
	@Override
	public void onPictureTaken(byte[] picture)
	{
		Log.d(TAG, "Picture Taken!!!");
		// TODO: Save file to temp storage and save in capturedFiles collection
	}

	/**
	 * Pause the preview stream from the camera
	 */
	public void pause()
	{
		switch (getState())
		{
			case PREVIEW_STARTED:
				setState(State.PAUSED);
				if (m_cameraSource != null)
				{
					m_cameraSource.stop();
				}
				break;

			case STOPPED:
				throw new DeviceStateException();

			case PAUSED:
				// do nothing
				break;
		}
	}

	/**
	 * Resume the preview from the camera
	 */
	public void resume()
	{
		switch (getState())
		{
			case PREVIEW_STARTED:
				// do nothing
				break;

			case STOPPED:
				throw new DeviceStateException();

			case PAUSED:
				startPreview();
				break;
		}
	}

	/**
	 * Stop the camera.
	 */
	public void stop()
	{
		if (m_cameraSource != null)
		{
			m_cameraSource.stop();
		}
		setState(State.STOPPED);
	}


	/**
	 * Add a CaptureView that will present the data captured by the device.  All viewers receive
	 * the same capture data.
	 *
	 * @param viewer
	 * @throws InvalidDeviceBuffer
	 */
	@Override
	public void addViewer(CaptureView viewer) throws InvalidDeviceBuffer
	{
		if (viewer == null)
		{
			throw new IllegalArgumentException("Viewer cannot be null");
		}

		// see if we already have a SurfaceView in the viewer list.  We can only have one
		for (CaptureView view : mViewerList)
		{
			View v = view.getView();
			if (v instanceof SurfaceView)
			{
				if (viewer.getView() instanceof SurfaceView)
				{
					throw new IllegalArgumentException("View list cannot contain more than one SurfaceView.");
				}
				break;	// bail out here since we can only have one SurfaceView and this was it.
			}
		}

		// make sure the buffer tied to the view is a FaceDetectorBuffer and add the viewer to the list.
		FaceDetectorBuffer buffer = null;
		try
		{
			buffer = (FaceDetectorBuffer)viewer.getCaptureBuffer();	// test to make sure this is a valid face previwer
			mViewerList.add(viewer);
		}
		catch (ClassCastException ccex)
		{
			String[] args = new String[2];
			args[0] = "com.openbiometrix.capture.FaceDetectorBuffer";
			args[1] = buffer.getClass().getName();
			throw new InvalidDeviceBuffer(args);
		}
	}



	//==============================================================================================
	// Graphic Face Tracker
	//==============================================================================================

	/**
	 * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
	 * uses this factory to create face trackers as needed -- one for each individual.
	 */
	private class FaceTrackerFactory implements MultiProcessor.Factory<Face>
	{
		@Override
		public Tracker<Face> create(Face face) {
			return new FaceTracker();
		}
	}

	/**
	 * Face tracker for each detected individual. This maintains a face graphic within the app's
	 * associated face overlay.
	 */
	private class FaceTracker extends Tracker<Face>
	{
		FaceTracker()
		{

		}

		/**
		 * Start tracking the detected face instance within the face overlay.
		 */
		@Override
		public void onNewItem(int faceId, Face item)
		{
			// nothing to do here for simple face detection.  The detector only tracks the
			// most prominent face in the view.
		}

		/**
		 * Update the position/characteristics of the face in the viewer.
		 */
		@Override
		public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face)
		{
			try
			{
				for (CaptureView viewer : mViewerList)
				{
					viewer.getCaptureBuffer().put(face);

				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		/**
		 * Event called when a face goes missing.
		 */
		@Override
		public void onMissing(FaceDetector.Detections<Face> detectionResults)
		{
			// nothing to do here for simple face detection.
		}

		/**
		 * Called when the face is assumed to be gone for good. Remove the graphic annotation from
		 * the overlay.
		 */
		@Override
		public void onDone()
		{
			// nothing to do here for simple face detection
		}
	}

	private CameraSource m_cameraSource = null;
}
