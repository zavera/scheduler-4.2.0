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

import edu.harvard.catalyst.scheduler.AbstractSpringWiredHibernateTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author carl
 * @date Aug 1, 2013
 *
 */
public class SearchDTOTest extends AbstractSpringWiredHibernateTest {

    @Test
    public void testSearchType() {

        SearchDTO.SearchType searchType = SearchDTO.SearchType.ANY;
        assertEquals("any", searchType.getType());

        searchType = SearchDTO.SearchType.EXACT;
        assertEquals("exact", searchType.getType());

    }

    @Test
    public void testAttributeAccessors() {

        SearchDTO searchDTO = new SearchDTO();
        List<SearchDTO.SearchItem> searchItems = new ArrayList<>();
        searchDTO.setSearchItems(searchItems);

        assertEquals(searchItems, searchDTO.getSearchItems());

    }

    @Test
    public void testvisitTypeValueMapper() {
        String key = "foo";
        String value = " O  u t p a t i e n t C R C";

        SearchDTO.SearchItem searchItem = new SearchDTO.SearchItem();
        searchItem.setKey(key);
        searchItem.setValue(value);

        String result = SearchDTO.visitTypeValueMapper.apply(searchItem.getValue());
        assertEquals(" O utpatientCRC", result);

    }

    @Test
    public void testEncrypterValueMapper() {

    }


    @Test
    public void testToSearchClause() {

        // CASE 1 : empty search item list.
        // No specified key and value mappers.
        // Expect an empty string.

        SearchDTO searchDTO = new SearchDTO();
        List<SearchDTO.SearchItem> searchItems = new ArrayList<>();
        searchDTO.setSearchItems(searchItems);

        String whereClause = searchDTO.toSearchClause();

        assertEquals("", whereClause);

        // CASE 2 : a non-empty search list, with "OR search items".
        // Search type not specified on the search items.
        // No specified key and value mappers.
        // Expect a 3-value OR clause, with 'LIKE' comparisons

        searchDTO = new SearchDTO();
        searchItems = new ArrayList<>();
        searchDTO.setSearchItems(searchItems);

        // This search item will be ignored because we are going to set up a list or ORed search items
        SearchDTO.SearchItem searchItem1 = new SearchDTO.SearchItem();
        searchItem1.setKey("key1");
        searchItem1.setValue(" value1  ");

        List<SearchDTO.SearchItem> orSearchItems = new ArrayList<>();

        SearchDTO.SearchItem searchItem1a = new SearchDTO.SearchItem();
        searchItem1a.setKey("key1a");
        searchItem1a.setValue("value1a");
        orSearchItems.add(searchItem1a);

        SearchDTO.SearchItem searchItem1b = new SearchDTO.SearchItem();
        searchItem1b.setKey("key1b");
        searchItem1b.setValue("value1b");
        orSearchItems.add(searchItem1b);

        searchItem1.setOrSearchItems(orSearchItems);
        searchItems.add(searchItem1);

        whereClause = searchDTO.toSearchClause();

        assertEquals(" AND (key1a LIKE :key1a OR key1b LIKE :key1b)", whereClause);

        assertEquals(null, searchItem1.getQueryParamName());
        assertEquals("key1a", searchItem1a.getQueryParamName());
        assertEquals("key1b", searchItem1b.getQueryParamName());

        assertEquals(null, searchItem1.getQueryParamValue());
        assertEquals("%value1a%", searchItem1a.getQueryParamValue());
        assertEquals("%value1b%", searchItem1b.getQueryParamValue());

        // CASE 3: Same as 2 but specify search type as ANY and EXACT on different search items

        searchDTO = new SearchDTO();
        searchItems = new ArrayList<>();
        searchDTO.setSearchItems(searchItems);

        searchItem1 = new SearchDTO.SearchItem();
        searchItem1.setKey("key1");
        searchItem1.setValue(" value1  ");
        searchItem1.setSearchType(SearchDTO.SearchType.EXACT);

        orSearchItems = new ArrayList<>();

        searchItem1a = new SearchDTO.SearchItem();
        searchItem1a.setKey("key1a");
        searchItem1a.setValue("value1a");
        searchItem1a.setSearchType(SearchDTO.SearchType.ANY);
        orSearchItems.add(searchItem1a);

        searchItem1b = new SearchDTO.SearchItem();
        searchItem1b.setKey("key1b");
        searchItem1b.setValue("value1b");
        searchItem1b.setSearchType(SearchDTO.SearchType.EXACT);
        orSearchItems.add(searchItem1b);

        searchItem1.setOrSearchItems(orSearchItems);
        searchItems.add(searchItem1);

        whereClause = searchDTO.toSearchClause();

        assertEquals(" AND (key1a LIKE :key1a OR key1b = :key1b)", whereClause);

        assertEquals(null, searchItem1.getQueryParamName());
        assertEquals("key1a", searchItem1a.getQueryParamName());
        assertEquals("key1b", searchItem1b.getQueryParamName());

        assertEquals(null, searchItem1.getQueryParamValue());
        assertEquals("%value1a%", searchItem1a.getQueryParamValue());
        assertEquals("value1b", searchItem1b.getQueryParamValue());

        // CASE 4: One more search element than CASE 3, and prepend with " WHERE " and join with " OR "

        searchDTO = new SearchDTO();
        searchItems = new ArrayList<>();
        searchDTO.setSearchItems(searchItems);

        searchItem1 = new SearchDTO.SearchItem();
        searchItem1.setKey("key1");
        searchItem1.setValue(" value1  ");
        searchItem1.setSearchType(SearchDTO.SearchType.EXACT);

        orSearchItems = new ArrayList<>();

        searchItem1a = new SearchDTO.SearchItem();
        searchItem1a.setKey("key1a");
        searchItem1a.setValue("value1a");
        searchItem1a.setSearchType(SearchDTO.SearchType.ANY);
        orSearchItems.add(searchItem1a);

        searchItem1b = new SearchDTO.SearchItem();
        searchItem1b.setKey("key1b");
        searchItem1b.setValue("value1b");
        searchItem1b.setSearchType(SearchDTO.SearchType.EXACT);
        orSearchItems.add(searchItem1b);

        searchItem1.setOrSearchItems(orSearchItems);
        searchItems.add(searchItem1);

        SearchDTO.SearchItem searchItem2 = new SearchDTO.SearchItem();
        searchItem2.setKey("something.key2");
        searchItem2.setValue("value2");
        searchItem2.setSearchType(SearchDTO.SearchType.ANY);

        searchItems.add(searchItem2);

        whereClause = searchDTO.toSearchClause(" OR ", " WHERE ");

        assertEquals(" WHERE (key1a LIKE :key1a OR key1b = :key1b) OR something.key2 LIKE :something_key2", whereClause);

        assertEquals(null, searchItem1.getQueryParamName());
        assertEquals("key1a", searchItem1a.getQueryParamName());
        assertEquals("key1b", searchItem1b.getQueryParamName());
        assertEquals("something_key2", searchItem2.getQueryParamName());

        assertEquals(null, searchItem1.getQueryParamValue());
        assertEquals("%value1a%", searchItem1a.getQueryParamValue());
        assertEquals("value1b", searchItem1b.getQueryParamValue());
        assertEquals("%value2%", searchItem2.getQueryParamValue());

    }

    @Test
    public void testSearchClauseAtomListWithDefaultParams() {

        // would have used a Mockito spy, however does not work with static methods.
        // Instead, use PowerMockito, or do a real method call, as below

        SearchDTO searchDTO = new SearchDTO();
        List<SearchDTO.SearchItem> searchItems = new ArrayList<>();
        searchDTO.setSearchItems(searchItems);

        SearchDTO.SearchItem searchItem1 = new SearchDTO.SearchItem();
        searchItem1.setKey("v.visitType");
        searchItem1.setValue(" value ");
        searchItem1.setSearchType(SearchDTO.SearchType.ANY);
        searchItems.add(searchItem1);
        searchDTO.mapSearchItemKeyAndValue("v.visitType", "v.visitType", SearchDTO.visitTypeValueMapper);

        List<String> atomList = searchDTO.generateSearchClauseList();

        assertEquals(1, atomList.size());
        assertEquals("v.visitType LIKE :v_visitType", atomList.get(0));
        assertEquals("v_visitType", searchItem1.getQueryParamName());
        assertEquals("% value%", searchItem1.getQueryParamValue());

    }
}
