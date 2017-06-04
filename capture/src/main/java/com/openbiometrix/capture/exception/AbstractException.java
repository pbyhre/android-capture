package com.openbiometrix.capture.exception;

/**
 * Base Exception class to standardize exception format and information.
 *
 * Created by petebyhre on 4/10/17.
 */

public abstract class AbstractException extends RuntimeException
{
	/**
	 * Default constructor
	 */
	public AbstractException()
	{

	}

	/**
	 * Constructor that takes an errorCode
	 *
	 * @param errorCode
	 */
	public AbstractException(String errorCode)
	{
		setErrorCode(errorCode);
	}

	/**
	 * Constructor that takes an errorCode and arguements for the error.
	 *
	 * @param errorCode
	 * @param args
	 */
	public AbstractException(String errorCode, String[] args)
	{
		setErrorCode(errorCode);
		setArgs(args);
	}

	/**
	 * Sets the error code of the Exception
	 *
	 * @param errorCode
	 */
	public void setErrorCode(String errorCode)
	{
		this.errorCode = errorCode;
	}

	/**
	 * Gets the error code of the exception.
	 */
	public String getErrorCode()
	{
		return errorCode;
	}

	/**
	 * Sets the arguements of the exception.  These args are used as string replacements in the
	 * localized error message.
	 *
	 * @param args
	 */
	public void setArgs(String[] args)
	{
		this.args = args;
	}

	/**
	 * Gets the arguement list for the exception.
	 */
	public String[] getArgs()
	{
		return args;
	}


	private String errorCode = "error.unknown";
	private String[] args = null;
}
