/**
 * Copyright (c) 2015-2016, President and Fellows of Harvard College
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.harvard.catalyst.scheduler.dto;

import com.google.common.base.Joiner;
import edu.harvard.catalyst.scheduler.util.MiscUtil;
import org.hibernate.query.Query;
import org.hibernate.type.StringType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by marc-danie on 10/17/16.
 */
public class SearchDTO {
    List<SearchItem> searchItems;

    private Set<String> queryParamNames = new HashSet<>();

    /**
     * This is a wrapper around the real setter. It enforces that the query param name is unique
     * for the entire search dto.
     *
     * IMPORTANT: SearchItems' HQL parameter names are stored in the SearchDTO using the
     * queryParamNames set, to prevent collisions (i.e. when the same DB column can be matched
     * to multiple distinct values). As a result, if the same SearchDTO is used for multiple
     * queries, the generated parameter names will be different (they will have an extra "_"
     * appended to them). This is ok in principle, but makes testing trickier when running the
     * same query multiple times with the same search items. Also in application code,
     * The SearchDTO and SearchItems should be used to generate only one query and no more.
     *
     * @param searchItem
     */
    public void assignUniqueQueryParamName(SearchItem searchItem) {
        String newQueryParamName = searchItem.getKey().replace(".", "_").trim();
        while (queryParamNames.contains(newQueryParamName)) {
            newQueryParamName = newQueryParamName + "_";
        }
        queryParamNames.add(newQueryParamName);
        searchItem.setQueryParamName(newQueryParamName);
    }

    public enum SearchType {
        EXACT("exact"), ANY("any");

        private String type;

        SearchType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public SearchDTO(){}

    public List<SearchItem> setSearchItems(List<SearchItem> searchItems) {
        return this.searchItems = searchItems;
    }

    public List<SearchItem> getSearchItems() {
        return searchItems;
    }

    public static class SearchItem {
        String key;
        String value;
        String queryParamName;
        String queryParamValue;
        SearchType searchType;
        // If the list orSearchItems is not empty, then the above key/value information will be ignored
        List<SearchItem> orSearchItems;

        public SearchItem(){};

        // useful for writing slightly more compact tests
        public SearchItem(
                SearchType searchType,
                String key,
                String value,
                List<SearchItem> orSearchItems
        ){
            this.searchType = searchType;
            this.key = key;
            this.value = value;
            this.orSearchItems = orSearchItems;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setQueryParamName(String queryParamName) {
            this.queryParamName = queryParamName;
        }

        public String getQueryParamName() {
            return this.queryParamName;
        }

        public void setQueryParamValue(String queryParamValue) {
            this.queryParamValue = queryParamValue;
        }

        public String getQueryParamValue() {
            return this.queryParamValue;
        }

        public SearchType getSearchType() {
            return searchType;
        }

        public void setSearchType(SearchType searchType) {
            this.searchType = searchType;
        }

        public List<SearchItem> getOrSearchItems() {
            return orSearchItems;
        }

        public void setOrSearchItems(List<SearchItem> orSearchItems) {
            this.orSearchItems = orSearchItems;
        }

    }


    /**
     * This is the value mapper for the visit type. It removes single spaces following non-space strings
     *
     */
    public static final Function<String, String> visitTypeValueMapper = value ->
            value.replaceAll("(\\S) ", "$1");


    /**
     * This is a helper method to be used by the service layer, to map the keys and values returned by the
     * front-end to new keys and values to be used in the HQL query (i.e. values that match values of columns in the query).
     *
     * This method has side-effects on the SearchItems
     *
     * @param key
     * @param newKey
     * @param valueMapper
     */
    public void mapSearchItemKeyAndValue(String key, String newKey, Function<String, String> valueMapper) {

        List<SearchDTO.SearchItem> searchItems = this.getSearchItems();
        if (searchItems != null && searchItems.size() > 0) {
            searchItems.stream().forEach(si -> {
                if (si.getKey().equals(key)) {
                    // Handle either ORed search items, or a unique search item
                    List<SearchItem> orSearchItems = si.getOrSearchItems();
                    if (orSearchItems != null && orSearchItems.size() > 0) {
                        for (SearchItem orSearchItem: orSearchItems) {
                            orSearchItem.setKey(newKey);
                            if (valueMapper != null) {
                                orSearchItem.setValue(valueMapper.apply(orSearchItem.getValue()));
                            }
                        }
                    }
                    else {
                        si.setKey(newKey);
                        if (valueMapper != null) {
                            si.setValue(valueMapper.apply(si.getValue()));
                        }
                    }
                }

            });
        }

    }

    /**
     * This is a helper method to be used by the service layer, to map the keys and values returned by the
     * front-end to new keys and values to be used in the HQL query (i.e. values that match values of columns in the query).
     *
     * This method has side-effects on the SearchItems
     *
     * @param keyMapperMap
     * @param valueMapperMap
     */
    public void mapSearchItemKeysAndValues(Map <String, String> keyMapperMap, Map <String, Function<String, String>> valueMapperMap) {

        List<SearchDTO.SearchItem> searchItems = this.getSearchItems();
        if (searchItems != null && searchItems.size() > 0) {
            searchItems.stream().forEach(si -> {
                String key = si.getKey();
                if (key == null) {

                    // Handle either ORed search items, or a unique search item
                    List<SearchItem> orSearchItems = si.getOrSearchItems();

                    if (orSearchItems != null && orSearchItems.size() > 0) {
                        for (SearchItem orSearchItem : orSearchItems) {
                            String newKey = keyMapperMap.get(orSearchItem.getKey());
                            if(newKey != null){
                                Function<String, String> valueMapper = valueMapperMap.get(newKey);
                                orSearchItem.setKey(newKey);
                                if (valueMapper != null) {
                                    orSearchItem.setValue(valueMapper.apply(orSearchItem.getValue()));
                                }
                            }
                        }
                    }
                }
                else {
                    String newKey = keyMapperMap.get(si.getKey());
                    if(newKey != null){
                        si.setKey(newKey);

                        Function<String, String> valueMapper = valueMapperMap.get(newKey);
                        if (valueMapper != null) {
                            si.setValue(valueMapper.apply(si.getValue()));
                        }
                    }
                }
            });
        }

    }

    /**
     * Generates the essential information needed to write a HQL 'where' clause based on a list of SearchItem objects.
     *
     * @return
     */
    public List<String> generateSearchClauseList() {

        List<SearchDTO.SearchItem> searchItems = this.getSearchItems();
        List<String> searchClauses;
        if (searchItems == null || searchItems.size() == 0) {
            searchClauses = new ArrayList<>();
        }
        else {
            searchClauses = searchItems.stream()
                    .filter(si -> (MiscUtil.isNonNullNonEmpty(si.getValue())) || si.getOrSearchItems() != null)
                    .map(si -> {
                        List<SearchItem> orSearchItems = si.getOrSearchItems();
                        if (orSearchItems != null && orSearchItems.size() > 0) {
                            List<String> orSearchClauses = orSearchItems.stream()
                                    .map(osi -> {
                                        StringBuilder searchClauseBuilder = new StringBuilder(osi.getKey());
                                        this.generateAndAppendMatchClause(searchClauseBuilder, osi);
                                        return searchClauseBuilder.toString();
                                    }).collect(Collectors.toList());
                            return "(" + Joiner.on(" OR ").join(orSearchClauses) + ")";
                        }
                        else if (si.getValue() != null && !si.getValue().isEmpty()) {
                            StringBuilder searchClause = new StringBuilder(si.getKey());
                            this.generateAndAppendMatchClause(searchClause, si);
                            return searchClause.toString();
                        }
                        else {
                            // should never get here
                            return null;
                        }
                    }).collect(Collectors.toList());
        }
        return searchClauses;

    }


    public void generateAndAppendMatchClause(StringBuilder searchClause, SearchItem searchItem) {

        String matchClause;
        this.assignUniqueQueryParamName(searchItem);
        if (searchItem.getSearchType() == SearchType.EXACT) {
            matchClause = " = :" + searchItem.getQueryParamName();
            searchItem.setQueryParamValue(searchItem.getValue());
        } else {
            // This is the default even if the search type is null
            matchClause = " LIKE :" + searchItem.getQueryParamName();
            searchItem.setQueryParamValue("%" + searchItem.getValue() + "%");
        }
        searchClause.append(matchClause);

    }

    /**
     * Main method for generating a HQL 'where' clause. The reason for calling this method with
     * both the searchDTO and a list of searchItems is that traversal of the search items
     * is re-entrant, and so this method can be called with a searchDTO and a sub-list of
     * its searchItems, specifically when traversing the 'orSearchItems'
     *
     * @param joinString
     * @param prependString
     * @return
     */
    public String toSearchClause(
            final String joinString,
            final String prependString) {

        List<String> searchClauses = this.generateSearchClauseList();

        if (!searchClauses.isEmpty()) {
            return prependString + Joiner.on(joinString).join(searchClauses);
        }
        else {
            return "";
        }

    }

    /**
     * Overloads toSearchClause() to provide shortcuts
     *
     * @return
     */
    public String toSearchClause() {

        return this.toSearchClause(" AND ", " AND ");

    }

    public void assignQueryParameterValues(final Query query) {

        final List<SearchItem> searchItems = this.getSearchItems();
        if(MiscUtil.isNonNullNonEmpty(searchItems)) {
            searchItems.stream().forEach(searchItem -> {
                List<SearchItem> orSearchItems = searchItem.getOrSearchItems();
                if (MiscUtil.isNonNullNonEmpty(orSearchItems)) {
                    for (SearchItem orSearchItem : orSearchItems) {
                        query.setParameter(orSearchItem.getQueryParamName(), orSearchItem.getQueryParamValue(), StringType.INSTANCE);
                    }
                }
                else {
                    query.setParameter(searchItem.getQueryParamName(), searchItem.getQueryParamValue(), StringType.INSTANCE);
                }
            });
        }

    }

}
