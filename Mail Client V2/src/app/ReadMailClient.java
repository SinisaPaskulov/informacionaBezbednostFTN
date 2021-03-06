package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.xml.security.utils.JavaUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import support.MailHelper;
import support.MailReader;
import util.Base64;
import util.GzipUtil;
import xml.crypto.AsymmetricKeyDecryption;
import xml.crypto.AsymmetricKeyEncryption;
import xml.signature.SignEnveloped;
import xml.signature.VerifySignatureEnveloped;

public class ReadMailClient extends MailClient {

	public static long PAGE_SIZE = 3;
	public static boolean ONLY_FIRST_PAGE = true;
	
	private static final String KEY_FILE = "./data/session.key";
	private static final String IV1_FILE = "./data/iv1.bin";
	private static final String IV2_FILE = "./data/iv2.bin";
	
	public static void main(String[] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, MessagingException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        // Build a new authorized API client service.
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
    	
    	Document doc = AsymmetricKeyDecryption.loadDocument("./data/secret_message.xml");
    	
    	Certificate receiverCert = SignEnveloped.readCertificate(keyStorePath, jksPassword, username);
    	
    	if(receiverCert != null && doc != null) {
    		
    		PrivateKey privateKey = AsymmetricKeyDecryption.readPrivateKey(keyStorePath, jksPassword, username);
    		doc = AsymmetricKeyDecryption.decrypt(doc, privateKey);
    		
    		//String from = doc.getDocumentElement().getAttribute("from");
    		
    		//Certificate senderCert = AsymmetricKeyEncryption.readCertificate(keyStorePath, jksPassword, from);
    		
    		if(VerifySignatureEnveloped.verifySignature(doc)) {
    			printEmail(doc);
    		}else {
    			System.out.println("Invalid signature");
    		}
    		
    	}else {
        	System.out.println("Cant load receiver certificate");
        }
    	
        /*ArrayList<MimeMessage> mimeMessages = new ArrayList<MimeMessage>();
        
        String user = "me";
        String query = "is:unread label:INBOX";
        
        List<Message> messages = MailReader.listMessagesMatchingQuery(service, user, query, PAGE_SIZE, ONLY_FIRST_PAGE);
        for(int i=0; i<messages.size(); i++) {
        	Message fullM = MailReader.getMessage(service, user, messages.get(i).getId());
        	
        	MimeMessage mimeMessage;
			try {
				
				mimeMessage = MailReader.getMimeMessage(service, user, fullM.getId());
				
				System.out.println("\n Message number " + i);
				System.out.println("From: " + mimeMessage.getHeader("From", null));
				System.out.println("Subject: " + mimeMessage.getSubject());
				System.out.println("Body: " + MailHelper.getText(mimeMessage));
				System.out.println("\n");
				
				mimeMessages.add(mimeMessage);
	        
			} catch (MessagingException e) {
				e.printStackTrace();
			}	
        }
        
        System.out.println("Select a message to decrypt:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	        
	    String answerStr = reader.readLine();
	    Integer answer = Integer.parseInt(answerStr);
	    
		MimeMessage chosenMessage = mimeMessages.get(answer);
	    
        //TODO: Decrypt a message and decompress it. The private key is stored in a file.
		Cipher aesCipherDec = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKey secretKey = new SecretKeySpec(JavaUtils.getBytesFromFile(KEY_FILE), "AES");
		
		
		byte[] iv1 = JavaUtils.getBytesFromFile(IV1_FILE);
		IvParameterSpec ivParameterSpec1 = new IvParameterSpec(iv1);
		aesCipherDec.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec1);
		
		String str = MailHelper.getText(chosenMessage);
		byte[] bodyEnc = Base64.decode(str);
		
		String receivedBodyTxt = new String(aesCipherDec.doFinal(bodyEnc));
		String decompressedBodyText = GzipUtil.decompress(Base64.decode(receivedBodyTxt));
		System.out.println("Body text: " + decompressedBodyText);
		
		
		byte[] iv2 = JavaUtils.getBytesFromFile(IV2_FILE);
		IvParameterSpec ivParameterSpec2 = new IvParameterSpec(iv2);
		//inicijalizacija za dekriptovanje
		aesCipherDec.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec2);
		
		//dekompresovanje i dekriptovanje subject-a
		String decryptedSubjectTxt = new String(aesCipherDec.doFinal(Base64.decode(chosenMessage.getSubject())));
		String decompressedSubjectTxt = GzipUtil.decompress(Base64.decode(decryptedSubjectTxt));
		System.out.println("Subject text: " + new String(decompressedSubjectTxt));*/
	}
	
	// Ispisivanje poruke iz dokumenta
	public static void printEmail(Document doc) {
		Node fc = doc.getFirstChild();
		NodeList list = fc.getChildNodes();
		for (int i = 0; i <list.getLength(); i++) {
			Node node = list.item(i);
			if("subject".equals(node.getNodeName())) {
				System.out.println("Subject: " + node.getTextContent());
			}
			if("body".equals(node.getNodeName())) {
				System.out.println("Body: " + node.getTextContent());
			}
		}
	}
}
