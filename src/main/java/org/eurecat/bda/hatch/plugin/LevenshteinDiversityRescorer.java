package org.eurecat.bda.hatch.plugin;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LevenshteinDiversityRescorer implements DiversityRescorer {
    private final List<FieldMappingSpec> fieldMappings;
    private final float diversityFactor;

    public LevenshteinDiversityRescorer(List<FieldMappingSpec> fieldMapping, float diversityFactor) {
        this.fieldMappings = fieldMapping;
        this.diversityFactor = diversityFactor;
    }

    @Override
    public void rescore(SearchResponse searchResponse) {
        SearchHits hits = searchResponse.getHits();
        Set<Integer> originalResponse = new HashSet<Integer>();

        SearchHit[] searchHits = searchResponse.getHits().getHits();
        int docId = 0;
        for (SearchHit hit : searchHits) {
            originalResponse.add(docId);
            docId++;
        }
        Set<Integer> rescoredHists = new HashSet<Integer>();
        getMaxSimilarity(hits, originalResponse, rescoredHists);

    }

    private void getMaxSimilarity(SearchHits hits, Set<Integer> originalResponse, Set<Integer> rescoredHists) {
        if (originalResponse.size() == 0) {
            return;
        }
        float maxScore = 0;
        int selected_doc = -1;
        for (int doc : originalResponse){
            float relevanceScore = this.diversityFactor * hits.getAt(doc).getScore();
            float maxSimilarity = 0;
            for (int doc2: rescoredHists){
                float sim2 = calculateLevenshtein(getTitle(hits.getAt(doc)), getTitle(hits.getAt(doc2)));
                if (sim2 > maxSimilarity) {
                    maxSimilarity = sim2;
                }
            }
            float divScore = maxSimilarity * (1-this.diversityFactor);
            float totalScore = relevanceScore - divScore;
            if (totalScore >=  maxScore) {
                maxScore = totalScore;
                selected_doc = doc;
            }
        }
        originalResponse.remove(selected_doc);
        rescoredHists.add(selected_doc);
        hits.getAt(selected_doc).score(maxScore);
        getMaxSimilarity(hits, originalResponse, rescoredHists);
    }

    private String getTitle(SearchHit document) {
        for (FieldMappingSpec fieldMapping: fieldMappings) {
            if (fieldMapping.logicalField==LogicalField.TITLE){
                return (String)document.getSourceAsMap().get(fieldMapping.field);
            }
        }
        return null;
    }

    private float calculateLevenshtein(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1]
                                    + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }
        float result = 1.0f / ((dp[x.length()][y.length()]) + 1);
        return result;
    }

    private static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private int min(int... numbers) {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
    }
}
