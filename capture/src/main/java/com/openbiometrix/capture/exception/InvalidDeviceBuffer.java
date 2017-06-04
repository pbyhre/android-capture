package com.openbiometrix.capture.exception;

/**
 * There was an attempt to use a device buffer that is not the correct type given the CaptureDevice.
 *
 * Created by petebyhre on 4/10/17.
 */
public class InvalidDeviceBuffer extends AbstractException
{
	public InvalidDeviceBuffer(String[] args)
	{
		super("error.capture.invalid.device.buffer.type", args);
	}
}
