package com.openbiometrix.capture.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.openbiometrix.capture.R;
import com.openbiometrix.capture.ui.FaceCaptureFragment;

/**
 * Simple activity that uses the FaceCaptureFragment.
 *
 * Created by petebyhre on 4/10/17.
 */

public class FaceCaptureActivity extends AppCompatActivity implements FaceCaptureFragment.OnFragmentInteractionListener
{
	private final static String TAG = "FaceCaptureActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face_capture);
	}

	@Override
	public void onFragmentInteraction(Uri uri)
	{

	}

}
