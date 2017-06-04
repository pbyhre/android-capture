package com.openbiometrix.capture.exception;

/**
 * Created by petebyhre on 4/17/17.
 */

public class NoDeviceFoundException extends AbstractException
{
	public NoDeviceFoundException()
	{
		super("error.no.device.found");
	}
}
