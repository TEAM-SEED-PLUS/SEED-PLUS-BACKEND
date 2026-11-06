package seed.seedplusbackend.support.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seed.seedplusbackend.support.domain.entity.Notice;
import seed.seedplusbackend.support.domain.repository.NoticeRepository;

public interface NoticeJpaRepository extends JpaRepository<Notice, Long>, NoticeRepository {}
