package com.cert.task;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.cert.model.JKSFile;

import lombok.extern.java.Log;

@Log
public class KeystoreManagerTaskIntegrationTest {
	
	@Rule
    public TestName name = new TestName();
	
	private JKSFile jksFile1;
	private JKSFile jksFile2;
	
	private long start;
	
	private ExecutorService executorService; 
	
	 /**
	   * Test setup.
	   */
	@Before
	public void setup() {
		start = System.currentTimeMillis();
		char[] password = {'c', 'h', 'a', 'n', 'g', 'e', 'i', 't'};
		this.jksFile1 = createJKSFile("E:\\Java SDK 1.8\\lib\\security\\cacerts", password);
		this.jksFile2 = createJKSFile("E:\\Java JDK 10.0\\lib\\security\\cacerts", password);
	}
	

	/**
	   * Test clean-up.
	   */
	 @After
	    public void end() {
	        log.log(Level.INFO, String.format("Test %s took %s ms \n", name.getMethodName(), System.currentTimeMillis() - start));
	    }
	 
	 /**
	   * Test when two executions are triggered in a single thread.
	   */
	 @Test
	    public void testGivenExecutorIsSingleThreaded_whenTwoExecutionsAreTriggered_thenRunInSequence() throws InterruptedException, ExecutionException {
		   executorService = Executors.newSingleThreadExecutor();

		   final KeystoreManagerTask task1 = new KeystoreManagerTask(this.jksFile1);
		   final KeystoreManagerTask task2 = new KeystoreManagerTask(this.jksFile2);
		
	       final Future<Boolean> result1 = executorService.submit(task1);
	       final Future<Boolean> result2 = executorService.submit(task2);

	        while (!result1.isDone() || !result2.isDone()) {
	            log.log(Level.INFO, String.format("Task 1 is %s and Task 2 is %s.", result1.isDone() ? "done" : "not done", result2.isDone() ? "done" : "not done"));

	            Thread.sleep(300);
	        }

	        assertEquals(true, result1.get().booleanValue());
	        assertEquals(true, result2.get().booleanValue());
	        
	        awaitTerminationAfterShutdown(executorService);
	    }
	 
	 /**
	   * Test for timeout.
	   */
	 @Test(expected = TimeoutException.class)
	    public void testWhenGetWithTimeoutLowerThanExecutionTime_thenThrowException() throws InterruptedException, ExecutionException, TimeoutException {
		 executorService = Executors.newSingleThreadExecutor();
		 
		 final KeystoreManagerTask task = new KeystoreManagerTask(this.jksFile1);
		 
		 final Future<Boolean> result = executorService.submit(task);
		
		 result.get(500, TimeUnit.MILLISECONDS);
		 
		 awaitTerminationAfterShutdown(executorService);
	    }

	 
	 /**
	   * Test when two executions are triggered in parallel.
	   */
	 @Test
	    public void testGivenExecutorIsMultiThreaded_whenTwoExecutionsAreTriggered_thenRunInParallel() throws InterruptedException, ExecutionException {
		   executorService = Executors.newFixedThreadPool(2);
		 
		   final KeystoreManagerTask task1 = new KeystoreManagerTask(this.jksFile1);
		   final KeystoreManagerTask task2 = new KeystoreManagerTask(this.jksFile2);
		 
	       final Future<Boolean> result1 = executorService.submit(task1);
	       final Future<Boolean> result2 = executorService.submit(task2);

	        while (!result1.isDone() || !result2.isDone()) {
	            log.log(Level.INFO, String.format("Task 1 is %s and Task 2 is %s.", result1.isDone() ? "done" : "not done", result2.isDone() ? "done" : "not done"));

	            Thread.sleep(300);
	        }

	        assertEquals(true, result1.get().booleanValue());
	        assertEquals(true, result2.get().booleanValue());
	        
	        awaitTerminationAfterShutdown(executorService);
	    }
	
	
	 /**
	   * Test future cancellation.
	   */
	 @Test(expected = CancellationException.class)
	    public void testWhenCancelFutureAndCallGet_thenThrowException() throws InterruptedException, ExecutionException, TimeoutException {
		   executorService = Executors.newSingleThreadExecutor();
		 
		   final KeystoreManagerTask task = new KeystoreManagerTask(this.jksFile1);
		 
	        final Future<Boolean> result = executorService.submit(task);

	        boolean canceled = result.cancel(true);

	        assertTrue("Future was canceled", canceled);
	        assertTrue("Future was canceled", result.isCancelled());

	        result.get();
	        
	        awaitTerminationAfterShutdown(executorService);
	    }
	 
	 /**
	   * Test multiple threads. with involeAll
	   */
	 @Test
	    public void testGivenMultipleThreads_whenInvokeAll_thenMainThreadShouldWaitForAllToFinish() {

	        executorService = Executors.newFixedThreadPool(10);

	        List<Callable<Boolean>> callables = Arrays.asList(
	            new KeystoreManagerTask(this.jksFile1), 
	            new KeystoreManagerTask(this.jksFile2));

	        try {
	            long startProcessingTime = System.currentTimeMillis();
	            List<Future<Boolean>> futures = executorService.invokeAll(callables);
	            
	            awaitTerminationAfterShutdown(executorService);

	            try {
	            	executorService.submit((Callable<Boolean>) () -> {
	                    Thread.sleep(1000000);
	                    return null;
	                });
	            } catch (RejectedExecutionException e) {
	            	log.log(Level.SEVERE, "An error ocurred: {0}", e.getMessage());
	            }

	            long totalProcessingTime = System.currentTimeMillis() - startProcessingTime;
	            assertTrue(totalProcessingTime >= 5000);

	            Boolean firstThreadResponse = futures.get(0)
	                .get();
	            assertTrue("First response should be from the fast thread", firstThreadResponse);

	            Boolean secondThreadResponse = futures.get(1)
	                .get();
	            assertTrue("Last response should be from the slow thread", secondThreadResponse);

	        } catch (ExecutionException | InterruptedException e) {
	        	log.log(Level.SEVERE, "An error ocurred: {0}", e.getMessage());
	        }       
	    }
	 
	 /**
	   * Test multiple threads. with CompletionService.
	   */
	 @Test
	    public void testGivenMultipleThreads_whenUsingCompletionService_thenMainThreadShouldWaitForAllToFinish() {

	        final CompletionService<Boolean> service = new ExecutorCompletionService<>(executorService);

	        List<Callable<Boolean>> callables = Arrays.asList(
	            new KeystoreManagerTask(this.jksFile1), 
	            new KeystoreManagerTask(this.jksFile2));

	        for (final Callable<Boolean> callable : callables) {
	            service.submit(callable);
	        }

	        try {

	            long startProcessingTime = System.currentTimeMillis();

	            Future<Boolean> future = service.take();
	            Boolean firstThreadResponse = future.get();
	            long totalProcessingTime = System.currentTimeMillis() - startProcessingTime;

	            assertTrue("First response should be from the fast thread", firstThreadResponse);
	            assertTrue(totalProcessingTime >= 100 && totalProcessingTime < 1000);
	            log.log(Level.INFO, "Thread finished after: {0} milliseconds", totalProcessingTime);

	            future = service.take();
	            Boolean secondThreadResponse = future.get();
	            totalProcessingTime = System.currentTimeMillis() - startProcessingTime;

	            assertTrue("Last response should be from the slow thread", secondThreadResponse);
	            assertTrue(totalProcessingTime >= 3000 && totalProcessingTime < 4000);
	            log.log(Level.INFO, "Thread finished after: {0} milliseconds", totalProcessingTime);

	        } catch (ExecutionException | InterruptedException e) {
	        	log.log(Level.SEVERE, "An error ocurred: {0}", e.getMessage());
	        } finally {
	            awaitTerminationAfterShutdown(executorService);
	        }
	    }
	 

	 /**
	   * Utility methods.
	   */
	 
	 /**
	   * Shut down executor service.
	   */
	 public void awaitTerminationAfterShutdown(final ExecutorService threadPool) {
	        threadPool.shutdown();
	        try {
	            if (!threadPool.awaitTermination(1, TimeUnit.SECONDS)) {
	                threadPool.shutdownNow();
	            }
	        } catch (InterruptedException ex) {
	            threadPool.shutdownNow();
	            Thread.currentThread().interrupt();
	        }
	    }
	 
	
	 /**
	   * Create jks files.
	   */
	 private JKSFile createJKSFile(final String filePath, final char[] password) {
		 final JKSFile jksFile = JKSFile.builder()
					.pathToStore(filePath)
					.passwordArray(password)
					.build();
		    assertNotNull(jksFile);
			return jksFile;
		}


}