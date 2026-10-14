package seed.seedplusbackend.support.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.support.domain.entity.Notice;
import seed.seedplusbackend.support.domain.entity.NoticeCategory;
import seed.seedplusbackend.support.domain.repository.NoticeRepository;

@Service
@RequiredArgsConstructor
public class NoticeQueryService {

  private final NoticeRepository noticeRepository;

  @Transactional(readOnly = true)
  public Page<Notice> getNotices(NoticeCategory category, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    if (category == null) {
      return noticeRepository.findAllByOrderByPinnedDescPublishedAtDesc(pageable);
    }
    return noticeRepository.findByCategoryOrderByPinnedDescPublishedAtDesc(category, pageable);
  }

  @Transactional(readOnly = true)
  public Notice getNotice(Long noticeId) {
    return noticeRepository
        .findById(noticeId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_NOTICE));
  }
}
