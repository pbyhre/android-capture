package com.openbiometrix.capture;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This is base CaptureBuffer that is used to transmit captured data from CaptureDevices to
 * CaptureViews.  The CaptureBuffer is implmenented as an ArrayBlockingQueue of type T items.
 *
 * Created by petebyhre on 4/10/17.
 */
public class CaptureBuffer<T>
{
	public final static int DEFAULT_BUFFER_QUEUE_CAPACITY = 100;
	public final static long DEFAULT_PUT_TIMEOUT_MS = 50;
	public final static long DEFAULT_TAKE_TIMEOUT_MS = 200;


	/**
	 * Create a CaptureBuffer with the default values.
	 */
	public CaptureBuffer()
	{
		setQueueCapacity(DEFAULT_BUFFER_QUEUE_CAPACITY);
	}

	/**
	 * Create a CaptureBuffer with a specified capacity.
	 *
	 * @param capacity
	 */
	public CaptureBuffer(int capacity)
	{
		setQueueCapacity(capacity);
	}

	/**
	 * Set the capacity of the buffer.
	 *
	 * @param capacity
	 */
	public void setQueueCapacity(int capacity)
	{
		mQueue = new ArrayBlockingQueue<T>(capacity);
	}

	/**
	 * Put an item in the buffer.  If the buffer is at capacity, then it will wait.
	 *
	 * @param buffer
	 * @throws InterruptedException
	 */
	public void put(T buffer) throws InterruptedException
	{
		if (mQueue != null)
		{
			try
			{
				mQueue.put(buffer);
				mPutSuccessCount++;
			}
			catch (InterruptedException ex)
			{
				mPutFailCount++;
				throw ex;
			}
		}
	}

	/**
	 * Put an item in the buffer.  If the buffer is at capacity, it will wait up to timeout
	 * milliseconds to attempt to complete.
	 *
	 * @param buffer
	 * @param timeout
	 * @throws InterruptedException
	 */
	public void put(T buffer, long timeout) throws InterruptedException
	{
		if (mQueue != null)
		{
			if (mQueue.offer(buffer, timeout, TimeUnit.MILLISECONDS) == true)
			{
				mPutSuccessCount++;
			}
			else
			{
				mPutFailCount++;
			}
		}
	}

	/**
	 * Gets the oldest item from the buffer and returns it.  The item is removed from the buffer.
	 * If there are no items in the buffer, the call blocks until there is an item to get.
	 *
	 * @return The oldest item in the queue.
	 * @throws InterruptedException
	 */
	public T take() throws InterruptedException
	{
		if (mQueue != null)
		{
			return mQueue.take();
		}
		return null;
	}

	/**
	 * Get the oldest item in the buffer, but only wait until the default timeout expires.
	 *
	 * @return The oldest item in the queue or null if timeout expires.
	 * @throws InterruptedException
	 */
	public T poll() throws InterruptedException
	{
		if (mQueue != null)
		{
			return mQueue.poll(DEFAULT_TAKE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		}
		return null;
	}

	/**
	 * Check to see if the buffer is empty.
	 *
	 * @return true if the buffer is empty, else false.
	 */
	public boolean isEmpty()
	{
		return mQueue.isEmpty();
	}

	/**
	 * Returns the count of packets that we failed to put on the queue.
	 */
	public long getLostPackets()
	{
		return mPutFailCount;
	}

	/**
	 * The percentage of packets that were not able to be put on the buffer.
	 * @return
	 */
	public float getLostPacketPct()
	{
		return ((float) mPutFailCount /(float) mPutSuccessCount) * 100.0f;
	}


	private ArrayBlockingQueue<T> 	mQueue = null;
	private long 					mPutSuccessCount = 0;
	private long 					mPutFailCount = 0;
}
