package com.smconsole.incident;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidentStatusHistoryRepository extends JpaRepository<IncidentStatusHistory, Long> {
    List<IncidentStatusHistory> findByIncidentId(Long incidentId);

}
