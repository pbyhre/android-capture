package com.openbiometrix.capture.ui;

import android.view.View;

import com.openbiometrix.capture.CaptureBuffer;
import com.openbiometrix.capture.CaptureDevice;

/**
 * Simple interface to a generic view used for capturing biometrics.  Classes using this interface
 * will contain a View that can be used to display the capture stream and / or other data.  It
 * contains a CaptureBuffer that is an implementation of a bounded queue that is used to transmit
 * capture data from the device to the views.
 *
 * Created by petebyhre on 4/10/17.
 */

public interface CaptureView<T extends CaptureBuffer<T>>
{
	/**
	 * A View that is used to display the capture stream and / or other data related to the capture.
	 */
	View getView();

	/**
	 * Returns an implementation of a capture buffer.  This is a bounded queue that is the
	 * pipeline for transmitted from the CaptureDevice.
	 *
	 * @return
	 */
	T getCaptureBuffer();

	/**
	 * State change notifier that alerts the CaptureView that there is a change in the state
	 * of the CaptureDevice
	 *
	 * @param state
	 */
	void onStateChanged(CaptureDevice.State state);
}
