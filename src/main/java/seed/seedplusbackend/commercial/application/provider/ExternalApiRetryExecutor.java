package seed.seedplusbackend.commercial.application.provider;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntToLongFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.global.error.ApplicationException;

@Component
public class ExternalApiRetryExecutor {

  public <T> T execute(
      Supplier<T> request,
      int maxRetryCount,
      Predicate<ApplicationException> retryable,
      IntToLongFunction backoffMillis,
      BiConsumer<Integer, ApplicationException> retryLogger,
      Function<InterruptedException, ApplicationException> interruptedException) {
    for (int retryCount = 0; ; retryCount++) {
      try {
        return request.get();
      } catch (ApplicationException exception) {
        if (!retryable.test(exception) || retryCount == maxRetryCount) {
          throw exception;
        }
        retryLogger.accept(retryCount, exception);
      }

      pause(backoffMillis.applyAsLong(retryCount), interruptedException);
    }
  }

  private void pause(
      long millis, Function<InterruptedException, ApplicationException> interruptedException) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw interruptedException.apply(exception);
    }
  }
}
