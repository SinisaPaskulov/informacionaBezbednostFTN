package app;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.security.utils.JavaUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.Gmail.Users.GetProfile;
import com.google.api.services.gmail.model.Profile;

import util.Base64;
import util.GzipUtil;
import util.IVHelper;
import xml.crypto.AsymmetricKeyEncryption;
import xml.signature.SignEnveloped;
import support.MailHelper;
import support.MailWritter;

public class WriteMailClient extends MailClient {

	private static final String KEY_FILE = "./data/session.key";
	private static final String IV1_FILE = "./data/iv1.bin";
	private static final String IV2_FILE = "./data/iv2.bin";
	
	public static void main(String[] args) {
		
        try {
        	//Gmail service = getGmailService();
        	
        	//Preuzimanje email adrese od prijavljenog korisnika
        	//String username = service.users().getProfile("me").execute().getEmailAddress();
        	System.out.println("username: ");
        	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        	String username = reader.readLine();
        	System.out.println("Welcome " + username);
        	
        	//Postavljanje putanje keystore fajla na osnovu email adrese prijavljenog korisnika
        	String keyStorePath = "./data/"+ username.substring(0,username.indexOf('@')) +".jks";
        	
        	System.out.println("Insert a jks file password for user '" + username + "': ");

            String jksPassword = reader.readLine();
            PrivateKey privateKey = SignEnveloped.readPrivateKey(keyStorePath, jksPassword, username);
            
        	System.out.println("Insert a reciever:");
            String reciever = reader.readLine();
        	
            System.out.println("Insert a subject:");
            String subject = reader.readLine();
                
            System.out.println("Insert body:");
            String body = reader.readLine();
            
            Document doc = createDocument(subject, body, username, reciever);
            
            Certificate senderCert = SignEnveloped.readCertificate(keyStorePath, jksPassword, username);
            
            if(senderCert != null) {
            	
                doc = SignEnveloped.signDocument(doc, privateKey, senderCert);
                
                SecretKey secretKey = AsymmetricKeyEncryption.generateDataEncryptionKey();
                
                Certificate receiverCert = AsymmetricKeyEncryption.readCertificate(keyStorePath, jksPassword, reciever);
                
                if(receiverCert != null) {
                    doc = AsymmetricKeyEncryption.encrypt(doc, secretKey, receiverCert);
                    
                    SignEnveloped.saveDocument(doc, "./data/secret_message.xml");
                    
                    System.out.println("Message sent successfully");
                    
                }else {
                	System.out.println("Cant load receiver certificate");
                }
            	
            }else {
            	System.out.println("Cant load your certificate");
            }
            
            /*
            //Compression
            String compressedSubject = Base64.encodeToString(GzipUtil.compress(subject));
            String compressedBody = Base64.encodeToString(GzipUtil.compress(body));
            
            //Key generation
            KeyGenerator keyGen = KeyGenerator.getInstance("AES"); 
			SecretKey secretKey = keyGen.generateKey();
			Cipher aesCipherEnc = Cipher.getInstance("AES/CBC/PKCS5Padding");
			
			//inicijalizacija za sifrovanje 
			IvParameterSpec ivParameterSpec1 = IVHelper.createIV();
			aesCipherEnc.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec1);
			
			
			//sifrovanje
			byte[] ciphertext = aesCipherEnc.doFinal(compressedBody.getBytes());
			String ciphertextStr = Base64.encodeToString(ciphertext);
			System.out.println("Kriptovan tekst: " + ciphertextStr);
			
			
			//inicijalizacija za sifrovanje 
			IvParameterSpec ivParameterSpec2 = IVHelper.createIV();
			aesCipherEnc.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec2);
			
			byte[] ciphersubject = aesCipherEnc.doFinal(compressedSubject.getBytes());
			String ciphersubjectStr = Base64.encodeToString(ciphersubject);
			System.out.println("Kriptovan subject: " + ciphersubjectStr);
			
			
			//snimaju se bajtovi kljuca i IV.
			//JavaUtils.writeBytesToFilename(KEY_FILE, secretKey.getEncoded());
			JavaUtils.writeBytesToFilename(IV1_FILE, ivParameterSpec1.getIV());
			JavaUtils.writeBytesToFilename(IV2_FILE, ivParameterSpec2.getIV());
			*/
        	/*MimeMessage mimeMessage = MailHelper.createMimeMessage(reciever, ciphersubjectStr, ciphertextStr);
        	MailWritter.sendMessage(service, "me", mimeMessage);*/
        	
        }catch (Exception e) {
        	e.printStackTrace();
		}
	}
	
	public static Document createDocument(String subject, String body, String from, String to) {
		
		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder icBuilder;
	    Document doc = null;
	    
	    try {
	    	icBuilder = icFactory.newDocumentBuilder();
	    	doc = icBuilder.newDocument();
	    	Element mainRootElement = doc.createElement("email");
	    	mainRootElement.setAttribute("from", from);
	    	mainRootElement.setAttribute("to", to);
	    	doc.appendChild(mainRootElement);
	    	
	    	/*Element fromNode = doc.createElement("from");
	    	fromNode.appendChild(doc.createTextNode(from));
	    	mainRootElement.appendChild(fromNode);
	    	
	    	Element toNode = doc.createElement("to");
	    	toNode.appendChild(doc.createTextNode(to));
	    	mainRootElement.appendChild(toNode);*/
	    	
	    	Element subjectNode = doc.createElement("subject");
	    	subjectNode.appendChild(doc.createTextNode(subject));
	    	mainRootElement.appendChild(subjectNode);
	    	
	    	Element bodyNode = doc.createElement("body");
	    	bodyNode.appendChild(doc.createTextNode(body));
	    	mainRootElement.appendChild(bodyNode);  	
	        
	    } catch (Exception e) {
	         e.printStackTrace();
	    }
	    
	    return doc;
	}
}
