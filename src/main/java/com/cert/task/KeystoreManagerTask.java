package com.cert.task;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import com.cert.keystore.KeystoreManager;
import com.cert.model.JKSFile;

import lombok.extern.java.Log;

@Log
public class KeystoreManagerTask implements Callable<Boolean> {

    private final JKSFile jksfile;

    /**
     * Creates a new KeystoreManagerTask.
     * 
     * @param jksfile - the keystore file.
     * 
     */
    public KeystoreManagerTask(final JKSFile jksfile) {
        this.jksfile = jksfile;       
    }

	@Override
	public Boolean call() throws Exception {
		 final File keyStoreFile = new File(jksfile.getPathToStore());
 		
		 KeystoreManager storeManager;
	        try {
	            storeManager = new KeystoreManager(keyStoreFile, jksfile.getPasswordArray());
	        } catch (NoSuchAlgorithmException e) {
	            log.log(Level.SEVERE, "The algorithm used for checking the integrity of the keystore cannot be found: {0}", e.getMessage());
	            return false;
	        } catch (CertificateException e) {
	            log.log(Level.SEVERE, "Could not load certificate from keystore: {0}", e.getMessage());
	            return false;
	        } catch (KeyStoreException e) {
	            log.log(Level.SEVERE, "Could not create copy of keystore: {0}", e.getMessage());
	            return false;
	        } catch (IOException e) {
	            log.log(Level.SEVERE, "Incorrect password provided: {0}", e.getMessage());
	            return false;
	        } catch (IllegalArgumentException e) {
	            log.log(Level.SEVERE, "An error ocurred: {0}", e.getMessage());
	            return false;
	        }  
	        
	        log.log(Level.INFO, "Started removing expired certificates from the keystore...");
	       
	        try {
	            storeManager.filterExpiredKeys();
	        } catch (KeyStoreException e) {
	            log.log(Level.SEVERE, "Error removing expired certificate from the store: {0}", e.getMessage());
	            return false;
	        }
  	
	        log.log(Level.INFO, "Started writing back to keystore...");
	        
	        try {
	            storeManager.save();
	        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
	            log.log(Level.SEVERE, "Unexpected error writing back to keystore: {0}", e.getMessage());
	            return false;
	        }
		
		return true;
	}

}