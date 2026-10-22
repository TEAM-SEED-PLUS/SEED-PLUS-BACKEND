package seed.seedplusbackend.support.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.support.domain.entity.Faq;
import seed.seedplusbackend.support.domain.entity.FaqCategory;
import seed.seedplusbackend.support.domain.entity.FaqGroup;

@Service
@RequiredArgsConstructor
public class FaqQueryService {

  private final FaqCatalog faqCatalog;

  @Transactional(readOnly = true)
  public Page<Faq> getFaqs(FaqGroup group, FaqCategory category, int page, int size) {
    List<Faq> filtered =
        faqCatalog.getFaqs().stream()
            .filter(faq -> group == null || faq.group() == group)
            .filter(faq -> category == null || faq.category() == category)
            .toList();
    Pageable pageable = PageRequest.of(page, size);
    int start = Math.min((int) pageable.getOffset(), filtered.size());
    int end = Math.min(start + pageable.getPageSize(), filtered.size());
    return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
  }
}
