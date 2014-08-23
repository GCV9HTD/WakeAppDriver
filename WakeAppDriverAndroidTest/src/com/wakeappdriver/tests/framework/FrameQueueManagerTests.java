package com.wakeappdriver.tests.framework;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.wakeappdriver.framework.FrameQueue;
import com.wakeappdriver.framework.FrameQueueManager;
import com.wakeappdriver.framework.dto.CapturedFrame;
import android.test.AndroidTestCase;

public class FrameQueueManagerTests  extends AndroidTestCase{
	FrameQueueManager frameQueueManager;
	FrameQueue frameQueue1;
	FrameQueue frameQueue2;
	
	@Before
	public void setUp() throws Exception {
		frameQueue1 = new FrameQueue(com.wakeappdriver.configuration.Enums.FrameQueueType.PERCENT_COVERED_QUEUE);
		frameQueue2 = new FrameQueue(com.wakeappdriver.configuration.Enums.FrameQueueType.YAWN_SIZE_QUEUE);
		List<FrameQueue> list = new ArrayList<FrameQueue>();
		list.add(frameQueue1);
		list.add(frameQueue2);
		frameQueueManager = new FrameQueueManager(list);
	}

	@Test
	public void testFrameQueueManager() {
		//test add and remove from manager
		for(int i = 0; i < 20; i++){
			CapturedFrame frame = new CapturedFrame(i, null, null);
			frameQueueManager.putFrame(frame);
		}
		
		for(int i = 0; i < 20; i++){
			CapturedFrame frame1 = frameQueueManager.PopFrame(frameQueue1);
			assertEquals(frame1.getTimestamp(), (long)i);
		}
		for(int i = 0; i < 20; i++){
			CapturedFrame frame2 = frameQueueManager.PopFrame(frameQueue2);
			assertEquals(frame2.getTimestamp(), (long)i);
		}
	}
}
