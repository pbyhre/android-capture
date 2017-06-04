package com.openbiometrix.capture.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.openbiometrix.capture.CaptureDevice;
import com.openbiometrix.capture.FaceCaptureDevice;
import com.openbiometrix.capture.R;

/**
 * This Fragment ties a FaceCaptureDevice to a FaceCaptureView that is defined in the layout.
 * In devices running 6.0 and greater, the user must grant permission to use the camera.
 *
 * Activities that contain this fragment must implement the
 * {@link FaceCaptureFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FaceCaptureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FaceCaptureFragment extends Fragment implements SurfaceHolder.Callback
{
	private final static String TAG = "FaceCaptureActivity";

	private static final int RC_HANDLE_CAMERA_PERM = 2;

	private FaceCaptureDevice mCamera = null;
	private FaceCaptureView mCameraPreview = null;
	private ImageView mNoCameraView = null;

	private OnFragmentInteractionListener mListener;

	public FaceCaptureFragment()
	{
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment.
	 *
	 * @return A new instance of fragment FaceCaptureFragment.
	 */
	public static FaceCaptureFragment newInstance(String param1, String param2)
	{
		return new FaceCaptureFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_face_capture, container, false);
		mNoCameraView = (ImageView) v.findViewById(R.id.noCameraView);
		mCameraPreview = (FaceCaptureView) v.findViewById(R.id.cameraView);
		mCameraPreview.getHolder().addCallback(this);

		mCameraPreview.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mCamera != null)
				{
					// make sure the camera is running before we try to take a picture
					if (mCamera.getState() == CaptureDevice.State.PREVIEW_STARTED)
					{
						mCamera.startCapture();
					}
				}
			}
		});

		// Check for the camera permission before accessing the camera.  If the
		// permission is not granted yet, request permission.
		int rc = ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.CAMERA);
		if (rc == PackageManager.PERMISSION_GRANTED)
		{
			// we have permission, setup the camera.
			initializeCamera();
		}
		else
		{
			// no permission, need to request it from user.
			requestCameraPermission();
		}
		// Inflate the layout for this fragment
		return v;
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri)
	{
		if (mListener != null)
		{
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener)
		{
			mListener = (OnFragmentInteractionListener) context;
		}
		else
		{
			throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener
	{
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}

	/**
	 * Fragment paused, we need to pause the camera.
	 */
	@Override
	public void onPause()
	{
		super.onPause();
		if (mCamera != null)
		{
			mCamera.pause();
		}
	}

	/**
	 * Fragment resumed.  We need to resume the camera if it was previously running.
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		if (mCamera != null)
		{
			mCamera.resume();
		}
	}

	/**
	 * Creates a FaceCaptureDevice using the default Front facing camera.
	 * Initializes the camera with the application context.
	 * Adds a FaceCaptureViewer to the device so we can see the images.
	 */
	private void initializeCamera()
	{
		mCamera = new FaceCaptureDevice();
		mCamera.initialize(this.getContext());
		mCamera.addViewer(mCameraPreview);
	}

	/**
	 * Handles the requesting of the camera permission.  This includes
	 * showing a "Snackbar" message of why the permission is needed then
	 * sending the request.
	 */
	private void requestCameraPermission()
	{
		Log.w(TAG, "FaceCaptureDevice permission is not granted. Requesting permission");

		final String[] permissions = new String[]{Manifest.permission.CAMERA};

		if (!ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), Manifest.permission.CAMERA))
		{
			ActivityCompat.requestPermissions(this.getActivity(), permissions, RC_HANDLE_CAMERA_PERM);
			return;
		}

		final Fragment thisFragment = this;

		View.OnClickListener listener = new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				requestPermissions(permissions, RC_HANDLE_CAMERA_PERM);
			}
		};

		Snackbar.make(mCameraPreview, R.string.permission_camera_rationale,
				Snackbar.LENGTH_INDEFINITE)
				.setAction(R.string.ok, listener)
				.show();
	}

	/**
	 * We got a result from a permissions request.  If permission was granted, then we need to
	 * initialize the camera and start showing the image.  If permission was denied, then we
	 * will pop up an image to show there is no camera.
	 *
	 * @param requestCode
	 * @param permissions
	 * @param grantResults
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		switch (requestCode)
		{
			case RC_HANDLE_CAMERA_PERM:
			{
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					// permission was granted.  setup the camera and start preview
					initializeCamera();
					mCamera.startPreview();
				}
				else
				{

					// permission denied.  show no camera
					mCameraPreview.setVisibility(View.GONE);
					mNoCameraView.setVisibility(View.VISIBLE);
				}
				return;
			}
		}
	}

	/**
	 * Event fired from the SurfaceView of the FaceCaptureView.  This means that we are ready
	 * to start showing data from the camera.
	 *
	 * @param surface
	 */
	@Override
	public void surfaceCreated(SurfaceHolder surface)
	{
		if (mCamera != null)
		{
			// surface is created.  we can start camera preview
			Log.d(TAG, "camera.startPreview()");
			mCamera.startPreview();
		}
	}

	/**
	 * Surface was destroyed, which means the FaceCaptureView is gone, so we can turn the camera off.
	 * @param surface
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder surface)
	{
		if (mCamera != null)
		{
			mCamera.stop();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		// no need to do anything here.
	}


}
