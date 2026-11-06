package seed.seedplusbackend.support.domain.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import seed.seedplusbackend.support.domain.entity.Notice;
import seed.seedplusbackend.support.domain.entity.NoticeCategory;

public interface NoticeRepository {
  Page<Notice> findAllByOrderByPinnedDescPublishedAtDesc(Pageable pageable);

  Page<Notice> findByCategoryOrderByPinnedDescPublishedAtDesc(
      NoticeCategory category, Pageable pageable);

  Optional<Notice> findById(Long id);
}
