package com.wakeappdriver.tests.framework;

import java.util.Queue;

import org.junit.Before;
import org.junit.Test;
import com.wakeappdriver.framework.ResultQueue;
import com.wakeappdriver.framework.dto.FrameAnalyzerResult;
import android.test.AndroidTestCase;

public class ResultQueueTests  extends AndroidTestCase{
	ResultQueue resultQueue;
	@Before
	public void setUp() throws Exception {
		resultQueue = new ResultQueue(com.wakeappdriver.configuration.Enums.FrameAnalyzerType.PERCENT_COVERED);
	}

	@Test
	public void testResultQueueSingle() {
		FrameAnalyzerResult resultBefore = new FrameAnalyzerResult(123.0, 456);
		resultQueue.add(resultBefore);
				
		Queue<FrameAnalyzerResult> results = resultQueue.getAll();
		
		assertEquals(results.size(),1);

		FrameAnalyzerResult resultAfter = results.remove();
		
		assertEquals(resultAfter.getValue(),123.0);
		assertEquals(resultAfter.getTimestamp(),456);
	}
	
	@Test
	public void testResultQueueEmpty() {
		Queue<FrameAnalyzerResult> results = resultQueue.getAll();
		
		assertEquals(results.size(),0);
	}
	
	@Test
	public void testResultQueueMultiple() {
		
		for (int i = 0; i < 30; i++){
			resultQueue.add(new FrameAnalyzerResult((double) i, i));
		}
		
		Queue<FrameAnalyzerResult> results = resultQueue.getAll();
		assertEquals(results.size(),30);
		
		Queue<FrameAnalyzerResult> newResults = resultQueue.getAll();
		assertEquals(newResults.size(),0);
		
		for (int i = 0; i < 30; i++){
			FrameAnalyzerResult result = results.remove();
			assertEquals(result.getValue(),(double)i);
			assertEquals(result.getTimestamp(),(long)i);
		}
	}
}
