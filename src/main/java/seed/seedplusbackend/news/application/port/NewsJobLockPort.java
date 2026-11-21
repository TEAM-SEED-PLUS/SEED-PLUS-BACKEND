package seed.seedplusbackend.news.application.port;

import java.util.UUID;

public interface NewsJobLockPort {
  boolean acquire(String job, UUID owner);

  void owned(String job, UUID owner, Runnable work);

  void release(String job, UUID owner);
}
