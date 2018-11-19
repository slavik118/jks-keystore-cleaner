package com.cert.util;

import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import static com.cert.util.Constants.CSV_FILE;

import org.junit.Test;
import org.junit.rules.TestName;

import lombok.extern.java.Log;

@Log
public class ConstantsTest {
	
	private long start;
	
	@Rule
    public TestName name = new TestName();
	
	/**
	   * Test setup.
	   */
	@Before
	public void setup() {
		start = System.currentTimeMillis();
	}
	
	/**
	   * Test clean-up.
	   */
	 @After
	    public void end() {
	        log.log(Level.INFO, String.format("Test %s took %s ms \n", name.getMethodName(), System.currentTimeMillis() - start));
	    }
	
	/**
	 * Test given file path constant.
   	*/
	@Test
	public void testCSV_FILEConstant() {
		assertEquals("assets/csv/jksfiles.csv", CSV_FILE);
	}

}
