package seed.seedplusbackend.payment.infrastructure.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.payment.application.port.BillingKeyEncryptionPort;

@Component
public class AesGcmBillingKeyEncryptionAdapter implements BillingKeyEncryptionPort {

  private static final int INITIALIZATION_VECTOR_LENGTH = 12;
  private static final int AUTHENTICATION_TAG_LENGTH = 128;

  private final BillingKeyEncryptionProperties properties;
  private final SecureRandom secureRandom = new SecureRandom();

  public AesGcmBillingKeyEncryptionAdapter(BillingKeyEncryptionProperties properties) {
    this.properties = properties;
  }

  @Override
  public String encrypt(String billingKey) {
    try {
      byte[] initializationVector = new byte[INITIALIZATION_VECTOR_LENGTH];
      secureRandom.nextBytes(initializationVector);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(
          Cipher.ENCRYPT_MODE,
          encryptionKey(),
          new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH, initializationVector));
      byte[] encrypted = cipher.doFinal(billingKey.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString(ByteBuffer.allocate(initializationVector.length + encrypted.length)
              .put(initializationVector)
              .put(encrypted)
              .array());
    } catch (GeneralSecurityException | IllegalArgumentException exception) {
      throw new ApplicationException(ErrorCode.BILLING_KEY_ENCRYPTION_FAILED, exception);
    }
  }

  @Override
  public String decrypt(String encryptedBillingKey) {
    try {
      byte[] encrypted = Base64.getUrlDecoder().decode(encryptedBillingKey);
      if (encrypted.length <= INITIALIZATION_VECTOR_LENGTH) {
        throw new ApplicationException(ErrorCode.BILLING_KEY_ENCRYPTION_FAILED, "invalid encrypted billing key", null);
      }
      ByteBuffer buffer = ByteBuffer.wrap(encrypted);
      byte[] initializationVector = new byte[INITIALIZATION_VECTOR_LENGTH];
      buffer.get(initializationVector);
      byte[] encryptedValue = new byte[buffer.remaining()];
      buffer.get(encryptedValue);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(
          Cipher.DECRYPT_MODE,
          encryptionKey(),
          new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH, initializationVector));
      return new String(cipher.doFinal(encryptedValue), StandardCharsets.UTF_8);
    } catch (GeneralSecurityException | IllegalArgumentException exception) {
      throw new ApplicationException(ErrorCode.BILLING_KEY_ENCRYPTION_FAILED, exception);
    }
  }

  private SecretKeySpec encryptionKey() {
    if (properties.secret() == null || properties.secret().isBlank()) {
      throw new ApplicationException(ErrorCode.BILLING_KEY_ENCRYPTION_FAILED, "encryption secret is missing", null);
    }
    byte[] decoded = Base64.getDecoder().decode(properties.secret());
    if (decoded.length != 32) {
      throw new ApplicationException(ErrorCode.BILLING_KEY_ENCRYPTION_FAILED, "invalid encryption secret", null);
    }
    return new SecretKeySpec(decoded, "AES");
  }
}
