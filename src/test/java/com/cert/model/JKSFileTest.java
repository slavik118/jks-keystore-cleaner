package com.cert.model;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.logging.Level;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import lombok.extern.java.Log;

@Log
public class JKSFileTest {
	
	private long start;

	private JKSFile jksFile;
	
	@Rule
    public TestName name = new TestName();

	/**
	   * Test setup.
	   */
	@Before
	public void setup() {
		start = System.currentTimeMillis();
		this.jksFile = createJKSFile();
	}
	
	/**
	   * Test clean-up.
	   */
	 @After
	    public void end() {
	        log.log(Level.INFO, String.format("Test %s took %s ms \n", name.getMethodName(), System.currentTimeMillis() - start));
	    }
	
	/**
	 * Test given CharArray hashcode when array elements value changed.
   	*/
	@Test
	public void testGivenCharArrayHashCode_WhenArrayElementsValueChanged_ThenHashCodesEqualAndValesNotEqual() {
		String originalHashCode = Integer.toHexString(jksFile.getPasswordArray().hashCode());

		Arrays.fill(jksFile.getPasswordArray(), '*');
		String changedHashCode = Integer.toHexString(jksFile.getPasswordArray().hashCode());

		assertThat(originalHashCode, is(changedHashCode));
		assertThat(jksFile.getPasswordArray(), is(not(new char[]{'p', 'a', 's', 's', 'w', 'o', 'r', 'd'})));
	}
	
	/**
	 * Test when calling toString method.
   	*/
	@Test
	public void testWhenCallingToStringOfString_ThenValuesEqual() {
		assertThat(jksFile.getPathToStore().toString(), is("E://"));
	}
	
	/**
	 * Test when calling toString of char array.
   	*/
	@Test
	public void testWhenCallingToStringOfCharArray_ThenValuesNotEqual() {
		assertThat(jksFile.getPasswordArray().toString(), is(not("password")));
	}


	/**
	 * Utility methods.
   	*/
	
	/**
	 * Create instance of the JKSFile class.
   	*/
	private JKSFile createJKSFile() {
		char[] charPassword = {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};
		final JKSFile jksFile = JKSFile.builder()
				.pathToStore("E://")
				.passwordArray(charPassword)
				.build();
		assertNotNull(jksFile);
		return jksFile;
	}
	
}
