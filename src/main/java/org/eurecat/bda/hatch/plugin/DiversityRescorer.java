package org.eurecat.bda.hatch.plugin;

import org.elasticsearch.action.search.SearchResponse;

public interface DiversityRescorer {
    void rescore(SearchResponse searchResponse);
}
