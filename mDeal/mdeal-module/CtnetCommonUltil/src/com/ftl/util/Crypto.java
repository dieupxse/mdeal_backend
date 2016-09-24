package com.ftl.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto
{
  public static final String DESEDE_KEY_ALGORITHM = "DESede";
  public static final String DESEDE_CIPHER_ALGORITHM = "DESede/CBC/PKCS5Padding";
  public static final String RSA_CIPHER_ALGORITHM = "RSA";
  public static final String RSA_KEY_ALGORITHM = "RSA";
  
  public static byte[] generate3DESKey()
    throws NoSuchAlgorithmException
  {
    KeyGenerator generator = KeyGenerator.getInstance("DESede");
    generator.init(new SecureRandom());
    return generator.generateKey().getEncoded();
  }
  
  public static byte[] generate3DESIV()
  {
    byte[] result = new byte[8];
    new SecureRandom().nextBytes(result);
    return result;
  }
  
  public static Cipher create3DESEncryptionCipher(byte[] secretKeyData, byte[] iv)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException
  {
    return create3DESEncryptionCipher(createSecretKey(secretKeyData, "DESede"), iv);
  }
  
  public static Cipher create3DESDecryptionCipher(byte[] secretKeyData, byte[] iv)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException
  {
    return create3DESDecryptionCipher(createSecretKey(secretKeyData, "DESede"), iv);
  }
  
  public static Cipher create3DESEncryptionCipher(SecretKey secretKey, byte[] iv)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException
  {
    Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
    cipher.init(1, secretKey, new IvParameterSpec(iv));
    return cipher;
  }
  
  public static Cipher create3DESDecryptionCipher(SecretKey secretKey, byte[] iv)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException
  {
    Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
    cipher.init(2, secretKey, new IvParameterSpec(iv));
    return cipher;
  }
  
  public static SecretKey createSecretKey(byte[] keyData, String algorithm)
  {
    return new SecretKeySpec(keyData, algorithm);
  }
  
  public static Cipher createRSAEncryptionCipher(byte[] publicKeyData)
    throws InvalidKeySpecException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
  {
    return createRSAEncryptionCipher(createPublicKey(publicKeyData, "RSA"));
  }
  
  public static Cipher createRSADecryptionCipher(byte[] privateKeyData)
    throws InvalidKeySpecException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
  {
    return createRSADecryptionCipher(createPrivateKey(privateKeyData, "RSA"));
  }
  
  public static Cipher createRSAEncryptionCipher(PublicKey publicKey)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException
  {
    return createRSAEncryptionCipher(publicKey, "RSA");
  }
  
  public static Cipher createRSAEncryptionCipher(PublicKey publicKey, String algorithm)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException
  {
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(1, publicKey);
    return cipher;
  }
  
  public static Cipher createRSADecryptionCipher(PrivateKey privateKey)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException
  {
    return createRSADecryptionCipher(privateKey, "RSA");
  }
  
  public static Cipher createRSADecryptionCipher(PrivateKey privateKey, String algorithm)
    throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException
  {
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(2, privateKey);
    return cipher;
  }
  
  public static PublicKey createPublicKey(byte[] keyData, String algorithm)
    throws NoSuchAlgorithmException, InvalidKeySpecException
  {
    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyData);
    KeyFactory kf = KeyFactory.getInstance(algorithm);
    return kf.generatePublic(publicKeySpec);
  }
  
  public static PrivateKey createPrivateKey(byte[] keyData, String algorithm)
    throws NoSuchAlgorithmException, InvalidKeySpecException
  {
    X509EncodedKeySpec privateKeySpec = new X509EncodedKeySpec(keyData);
    KeyFactory kf = KeyFactory.getInstance(algorithm);
    return kf.generatePrivate(privateKeySpec);
  }
  
  public static String encrypt(String source)
    throws Exception
  {
    MessageDigest md = MessageDigest.getInstance("SHA");
    md.update(source.getBytes("UTF-8"));
    byte[] raw = md.digest();
    return "";
  }
}
