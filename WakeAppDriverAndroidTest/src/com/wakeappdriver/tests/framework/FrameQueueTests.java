package com.wakeappdriver.tests.framework;

import org.junit.Before;
import org.junit.Test;

import com.wakeappdriver.framework.FrameQueue;
import com.wakeappdriver.framework.dto.CapturedFrame;
import android.test.AndroidTestCase;

public class FrameQueueTests  extends AndroidTestCase{
	FrameQueue frameQueue;

	@Before
	public void setUp() throws Exception {
		frameQueue = new FrameQueue(com.wakeappdriver.configuration.Enums.FrameQueueType.PERCENT_COVERED_QUEUE);
	}
	
	@Test
	public void testFrameQueueAdd() {
		assertEquals(frameQueue.isEmpty(), true);

		for (int i = 0; i < 20; i++ ){
			assertEquals(frameQueue.isFull(), false);

			assertEquals(frameQueue.tryAdd(new CapturedFrame(0, null, null)),true);
			
			assertEquals(frameQueue.isEmpty(), false);

		}
		assertEquals(frameQueue.isFull(), true);
		//test default max capacity of 20
		assertEquals(frameQueue.tryAdd(new CapturedFrame(0, null, null)),false);

	}
	
	@Test
	public void testFrameQueueEmpty() {
		//test that an exception is thrown if trying to remove from an empty queue
		boolean exception = false;
		try {
			frameQueue.remove();
		} catch (Exception e) {
			exception = true;
		}
		assertEquals(exception, true);
	}
	
}
