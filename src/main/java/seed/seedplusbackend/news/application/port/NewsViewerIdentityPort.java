package seed.seedplusbackend.news.application.port;

public interface NewsViewerIdentityPort {
  Identity resolve(Long userId, String cookie);

  record Identity(String viewerHash, String newCookie) {}
}
