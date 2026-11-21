package seed.seedplusbackend.news.application.port;

public interface NewsTelemetryPort {
  void count(String source, String outcome);

  void lag(String source, double seconds);
}
