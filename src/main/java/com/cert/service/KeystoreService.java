package com.cert.service;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.cert.model.JKSFile;
import com.cert.task.KeystoreManagerTask;
import com.cert.util.OpenCSVReader;

import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

import static com.cert.util.Constants.CSV_FILE;;

// When we apply @Service annotation on any class in Spring boot then Spring boot create a object of that
// class on application startup using default constructor of that class.
@Service
//  Service Bean should contain a default constructor.
@NoArgsConstructor
@Log
public class KeystoreService {

	@Autowired
	private ResourceLoader resourceLoader;	

	private Set<JKSFile> jksFiles = new HashSet<>();
	
	private ExecutorService executorService;
	 
	private volatile boolean stopThread = false;
	 
	// When we apply @PostConstruct annotation on init() method then init() method call after default constructor of Service Bean.
	// Create a method for load CSV file and put data of CSV file inside a ArrayList
	@PostConstruct
	public void init() throws KeyStoreException, NoSuchAlgorithmException, CertificateException {		
		log.log(Level.INFO, "Loading jks file paths from the jksfiles.csv file.");
		try {
			final Resource resource = resourceLoader.getResource("classpath:" + CSV_FILE);			
			jksFiles = OpenCSVReader.loadCSVFile(resource.getInputStream());
             if(jksFiles.size() > 0) {
            	 executorService = Executors.newFixedThreadPool(jksFiles.size());
            	 jksFiles.forEach(file -> {
            		// This is important to stop further indexing
            		 if(stopThread) return;
            		 
            		 log.log(Level.INFO, "JKS storage: {0} ", file.getPathToStore());
            		
            		 try {    			 
            		    final KeystoreManagerTask task = new KeystoreManagerTask(file);
            		 
            		    final Future<Boolean> future = executorService.submit(task);
        	
						boolean result = future.get().booleanValue();
						
						if(result) log.log(Level.INFO, "JKS storage {0} has been scanned successfully.", file.getPathToStore());
						
					} catch (InterruptedException | ExecutionException e) {
						log.log(Level.SEVERE, "An exception occurred: ", e.getMessage());
					}           		 
            		
            	 });
            	 
            	 // When finished using an ExecutorService, we need to shut it down explicitly.
            	 executorService.shutdown();
            	 
            	 log.log(Level.INFO, "The executor service has been shut down successfully.");
             }
    
		}catch (IOException e) {
		   log.log(Level.SEVERE, "An exception occurred: ", e.getMessage());
		}
	}
	
	// When we annotate a Spring Bean method with PreDestroy annotation, 
	// it gets called when bean instance is getting removed from the context. 
	@PreDestroy
	 public void shutdown() {
	  this.stopThread = true;
	  if(executorService != null){
	   try {
	    // Wait 1 second for closing all threads
	    executorService.awaitTermination(1, TimeUnit.SECONDS);
	    log.log(Level.INFO, "The executor service has been shut down.");
	   } catch (InterruptedException e) {
		   log.log(Level.SEVERE, "An error occured: ", e.getMessage());
	       Thread.currentThread().interrupt();
	   }
	  }
	 }
}
