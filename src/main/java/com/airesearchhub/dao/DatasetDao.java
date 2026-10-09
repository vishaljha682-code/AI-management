package com.airesearchhub.dao;

import com.airesearchhub.model.entity.Dataset;
import java.util.List;

public interface DatasetDao extends GenericDao<Dataset, Long> {
    List<Dataset> findByUploadedById(Long userId);
}
