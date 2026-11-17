package seed.seedplusbackend.payment.infrastructure.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@DisplayName("빌링 키 암호화")
class AesGcmBillingKeyEncryptionAdapterTest {

  @Test
  @DisplayName("동일한 mpKey도 매번 다른 암호문으로 저장한다")
  void encrypt_returnsDifferentCiphertexts() {
    AesGcmBillingKeyEncryptionAdapter adapter =
        new AesGcmBillingKeyEncryptionAdapter(
            new BillingKeyEncryptionProperties("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="));

    String first = adapter.encrypt("mp-key");
    String second = adapter.encrypt("mp-key");

    assertThat(first).isNotEqualTo("mp-key");
    assertThat(second).isNotEqualTo("mp-key");
    assertThat(first).isNotEqualTo(second);
    assertThat(adapter.decrypt(first)).isEqualTo("mp-key");
  }

  @Test
  @DisplayName("암호화 키가 없으면 저장을 거절한다")
  void encrypt_throwsExceptionWhenSecretIsMissing() {
    AesGcmBillingKeyEncryptionAdapter adapter =
        new AesGcmBillingKeyEncryptionAdapter(new BillingKeyEncryptionProperties(""));

    assertThatThrownBy(() -> adapter.encrypt("mp-key"))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(ErrorCode.BILLING_KEY_ENCRYPTION_FAILED));
  }
}
