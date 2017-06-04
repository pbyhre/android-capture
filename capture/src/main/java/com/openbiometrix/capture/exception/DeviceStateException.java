package com.openbiometrix.capture.exception;

/**
 * Exception that notifies that the caller attempted to do something to the CaptureDevice that
 * is invalid given its current state of operation.
 *
 * Created by petebyhre on 4/10/17.
 */
public class DeviceStateException extends AbstractException
{
	public DeviceStateException()
	{
		super("error.device.state");
	}

	public DeviceStateException(String errorCode)
	{
		super(errorCode);
	}
}
