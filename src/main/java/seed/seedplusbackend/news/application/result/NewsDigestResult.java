package seed.seedplusbackend.news.application.result;

import java.time.LocalDate;
import org.springframework.data.domain.Page;

public record NewsDigestResult(LocalDate digestDate, Page<NewsArticleResult> articles) {}
