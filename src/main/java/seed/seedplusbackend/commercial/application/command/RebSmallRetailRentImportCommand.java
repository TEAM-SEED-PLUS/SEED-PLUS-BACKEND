package seed.seedplusbackend.commercial.application.command;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import seed.seedplusbackend.commercial.application.provider.CommercialDataType;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

public record RebSmallRetailRentImportCommand(
    String originalFileName, byte[] fileContent, String fileHash, boolean force)
    implements CommercialDataCollectCommand {

  private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;

  public static RebSmallRetailRentImportCommand of(
      String originalFileName, byte[] fileContent, boolean force) {
    validate(originalFileName, fileContent);
    return new RebSmallRetailRentImportCommand(
        originalFileName, fileContent.clone(), sha256(fileContent), force);
  }

  @Override
  public CommercialDataType dataType() {
    return CommercialDataType.REB_SMALL_RETAIL_RENT;
  }

  @Override
  public String targetKey() {
    return fileHash;
  }

  @Override
  public byte[] fileContent() {
    return fileContent.clone();
  }

  private static void validate(String originalFileName, byte[] fileContent) {
    if (originalFileName == null
        || !originalFileName.toLowerCase().endsWith(".csv")
        || fileContent == null
        || fileContent.length == 0
        || fileContent.length > MAX_FILE_SIZE) {
      throw new ApplicationException(ErrorCode.REB_RENT_FILE_INVALID);
    }
  }

  private static String sha256(byte[] content) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }
}
