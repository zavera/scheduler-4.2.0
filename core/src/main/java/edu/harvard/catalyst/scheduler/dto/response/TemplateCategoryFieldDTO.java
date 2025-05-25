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
package edu.harvard.catalyst.scheduler.dto.response;

import java.util.Comparator;

/**
 * Created by ak303 on 11/13/2014.
 */
public class TemplateCategoryFieldDTO {
    private Integer id;
    private String categoryDisplayName;
    private String displayName;
    private boolean selected;
    private boolean sortable;
    private boolean filterable;
    private String fieldType;

    public TemplateCategoryFieldDTO(Integer id, String categoryDisplayName, String displayName,
                                    boolean selected, boolean sortable, boolean filterable, String fieldType) {
        this.id = id;
        this.categoryDisplayName = categoryDisplayName;
        this.displayName = displayName;
        this.selected = selected;
        this.sortable = sortable;
        this.filterable = filterable;
        this.fieldType = fieldType;
    }

    public Integer getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategoryDisplayName() {
        return categoryDisplayName;
    }

    public boolean getSelected() {
        return selected;
    }

    public boolean getSortable() {
        return sortable;
    }

    public boolean getFilterable() {
        return filterable;
    }

    public String getFieldType() {
        return fieldType;
    }

    public static class TemplateCategoryFieldsComparatorAsc implements Comparator<TemplateCategoryFieldDTO> {
        public int compare(TemplateCategoryFieldDTO o1, TemplateCategoryFieldDTO o2) {
            return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
        }
    }
}
