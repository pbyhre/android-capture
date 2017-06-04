package com.openbiometrix.capture;

import com.google.android.gms.vision.face.Face;

/**
 * Convenience class to define a CaptureBuffer of type Face
 *
 * Created by petebyhre on 4/10/17.
 */

public class FaceDetectorBuffer extends CaptureBuffer<Face>
{
	/**
	 * Default constructor
	 */
	public FaceDetectorBuffer()
	{
		super();
	}

	/**
	 * Constructor to set capacity of the buffer.
	 *
	 * @param capacity
	 */
	public FaceDetectorBuffer(int capacity)
	{
		super(capacity);
	}
}
