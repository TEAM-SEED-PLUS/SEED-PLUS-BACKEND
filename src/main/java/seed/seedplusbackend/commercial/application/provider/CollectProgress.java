package seed.seedplusbackend.commercial.application.provider;

@FunctionalInterface
public interface CollectProgress {

  void update(long totalCount, long fetchedCount, long cursor);
}
