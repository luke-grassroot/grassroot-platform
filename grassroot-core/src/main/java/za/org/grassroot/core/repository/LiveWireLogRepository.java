package za.org.grassroot.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.org.grassroot.core.domain.LiveWireLog;

/**
 * Created by luke on 2017/05/17.
 */
public interface LiveWireLogRepository extends JpaRepository<LiveWireLog, Long> {
}