package com.openbiometrix.capture;

import java.io.File;
import java.net.URI;

/**
 * Represents a file and mime type of the file that were captured.
 *
 * Created by petebyhre on 4/10/17.
 */

public class CaptureFile extends File
{
	/**
	 * Constructor that takes a path to the file
	 * @param pathname
	 */
	public CaptureFile(String pathname)
	{
		super(pathname);
	}

	/**
	 * Constructor that takes two strings that are the parent directory and child directory+file
	 *
	 * @param parent
	 * @param child
	 */
	public CaptureFile(String parent, String child)
	{
		super(parent, child);
	}

	/**
	 * Constructor that takes the file for the parent directory and the child directory+file.
	 * @param parent
	 * @param child
	 */
	public CaptureFile(File parent, String child)
	{
		super(parent, child);
	}

	/**
	 * Constructor that takes a URI to a file.
	 * @param uri
	 */
	public CaptureFile(URI uri)
	{
		super(uri);
	}

	/**
	 * Set the MIME media type of the file.
	 * @param mediaType
	 */
	public void setMediaType(String mediaType)
	{
		m_mediaType = mediaType;
	}

	/**
	 * Get the MIME media type of the file.
	 * @return
	 */
	public String getMediaType()
	{
		return m_mediaType;
	}


	private String m_mediaType = "*/*";
}
