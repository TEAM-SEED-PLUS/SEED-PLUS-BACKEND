package seed.seedplusbackend.news.infrastructure;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.global.error.*;
import seed.seedplusbackend.news.application.port.NewsViewerIdentityPort;

@Component
public class HmacNewsViewerIdentity implements NewsViewerIdentityPort {
  private final String secret;

  public HmacNewsViewerIdentity(@Value("${news.viewer.secret:}") String secret) {
    this.secret = secret;
  }

  @Override
  public Identity resolve(Long userId, String cookie) {
    if (secret.length() < 32) throw new ApplicationException(ErrorCode.NEWS_VIEWER_UNAVAILABLE);
    if (userId != null) return new Identity(sign("user:" + userId), null);
    String uuid = null;
    if (cookie != null && cookie.length() == 101) {
      String candidate = cookie.substring(0, 36);
      try {
        if (UUID.fromString(candidate).toString().equals(candidate)
            && cookie.charAt(36) == '.'
            && MessageDigest.isEqual(
                sign("cookie:" + candidate).getBytes(StandardCharsets.US_ASCII),
                cookie.substring(37).getBytes(StandardCharsets.US_ASCII))) uuid = candidate;
      } catch (IllegalArgumentException ignored) {
        /* 잘못된 쿠키는 새 익명 식별자로 교체한다. */
      }
    }
    if (uuid != null) return new Identity(sign("anonymous:" + uuid), null);
    uuid = UUID.randomUUID().toString();
    return new Identity(sign("anonymous:" + uuid), uuid + "." + sign("cookie:" + uuid));
  }

  private String sign(String value) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    } catch (java.security.GeneralSecurityException e) {
      throw new ApplicationException(ErrorCode.INTERNAL_SERVER_ERROR, e);
    }
  }
}
