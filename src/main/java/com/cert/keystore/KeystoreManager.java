package com.cert.keystore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.logging.Level;


import lombok.extern.java.Log;

@Log
public class KeystoreManager {

    private final KeyStore store;
    private final File file;
    private final char[] password;

    /**
     * Creates a new KeystoreManager.
     * 
     * @param file the keystore file.
     * @param password the password to access the file.
     * 
     * @throws NoSuchAlgorithmException if the algorithm used to check the integrity of the keystore cannot be found.
     * @throws CertificateException if any of the certificates in the keystore could not be loaded.
     * @throws IOException if a password is required but not given, or if the given password was incorrect. If the error is due to a wrong password, the cause of the IOException should be an UnrecoverableKeyException.
     * @throws KeyStoreException if a default keystore is unable to be created.
     * @throws IllegalArgumentException if the provided keystore or password were null or incorrect.
     */
    public KeystoreManager(File file, char[] password)
            throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {

        if (file == null) {
            throw new IllegalArgumentException("The keystore file cannot be null.");
        }
        if (password == null) {
            throw new IllegalArgumentException("The password cannot be null.");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException(String.format("A file wasn't found at %s.", file.getAbsolutePath()));
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException(
                    String.format("The provided file was invalid: %s.", file.getAbsolutePath()));
        }
        
        this.file = file;
        this.password = password;
        this.store = KeyStore.getInstance(KeyStore.getDefaultType());

        // Load store from jks file		
		try (final FileInputStream fis = new FileInputStream(file)) {
			 log.log(Level.INFO, "Loading keystore from file: {0}.", file.getAbsolutePath());
				
			 log.log(Level.INFO, "Total file size to read (in bytes) : {0}", fis.available());
			 store.load(fis, password);
			 
			 log.log(Level.INFO, "Keystore {0} loaded successfully.", file.getAbsolutePath());  
		}
    }

    /**
     * Removes all expired certificates from the store.
     * 
     * @throws KeyStoreException if there is an error removing a certificate from the store.
     */
    public void filterExpiredKeys() throws KeyStoreException {
        log.log(Level.INFO, "Removing expired keys.");
        int removedKeys = 0;       

        // Count through all aliases
        Enumeration<String> aliases = store.aliases();
        while (aliases.hasMoreElements()) {
            // Get the certificate and alias
            String alias = aliases.nextElement();
            Certificate cert = store.getCertificate(alias);

            // If the certificate is an X509 certificate
            if (cert.getType().equals("X.509")) {
                X509Certificate xCert = (X509Certificate) cert;
                // X.509 certificate properties 
                log.log(Level.INFO, "  Subject DN: {0}", xCert.getSubjectDN());
                log.log(Level.INFO, "  Signature Algorithm: {0}", xCert.getSigAlgName());
                log.log(Level.INFO, "  Valid from: {0}", xCert.getNotBefore());
                log.log(Level.INFO, "  Valid until: {0}", xCert.getNotAfter());
                log.log(Level.INFO, "  Issuer: {0}", xCert.getIssuerDN());
                
                String expiryDate = new SimpleDateFormat("dd/MM/yyyy").format(xCert.getNotAfter());

                log.log(Level.INFO, "Checking certificate {0} (expires {1}).", new Object[]{alias, expiryDate});
                // Check the X.509 certificate validity, and remove it if it's expired.
                try {
                    xCert.checkValidity();
                } catch (CertificateExpiredException | CertificateNotYetValidException e) {
                   // Remove expired X.509 certificate from the store
                    store.deleteEntry(alias);
                    log.log(Level.INFO, "Removed certificate {0} (expired {1}).", new Object[]{alias, expiryDate});
                    removedKeys++;
                }
            }
        }
        log.log(Level.INFO, "Successfully removed {0} expired keys out of a total {1}.",
                new Object[] { removedKeys, store.size() });
    }

    /**
     * Writes the keystore back to the given file.
     * 
     * @throws KeyStoreException if the keystore has not been initialized (loaded).
     * @throws IOException if there was an I/O problem with data.
     * @throws NoSuchAlgorithmException if the appropriate data integrity algorithm could not be found.
     * @throws CertificateException if any of the certificates included in the keystore data could not be stored.
     */
    public void save() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        log.log(Level.INFO, "Writing keystore to file: {0}.", file.getAbsolutePath());
		try (final FileOutputStream  out = new FileOutputStream(file)) {
			 store.store(out, password);
			 
			 log.log(Level.INFO, "Keystore {0} written successfully.", file.getAbsolutePath());
		} 
    }

}