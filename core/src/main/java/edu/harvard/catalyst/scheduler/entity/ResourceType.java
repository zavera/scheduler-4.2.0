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
package edu.harvard.catalyst.scheduler.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static edu.harvard.catalyst.hccrc.core.util.ListUtils.enrich;

public enum ResourceType implements HasId, HasReportFiltersNameAndId {

    Nursing(1),
    Nutrition(2),
    Room(3),
    Lab(4),
    Other(5);
    
    private int id;

    private ResourceType(final int id) {
        this.id = id;
    }

    @Override
    public int getReportFiltersId() {
        return ordinal();
    }
    @Override
    public String getReportFiltersName() {
        return name();
    }

    public boolean isNursing() {
        return this == Nursing;
    }
    
    public boolean isNutrition() {
        return this == Nutrition;
    }
    
    public boolean isRoom() {
        return this == Room;
    }
    
    public boolean isLab() {
        return this == Lab;
    }
    
    public boolean isOther() {
        return this == Other;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name();
    }

    @Override
    public String toString() {
        return "ResourceType [id=" + id + ", name=" + getName() + "]";
    }
    
    public static Optional<ResourceType> findById(final int id) {
        return enrich(values()).find(rt -> rt.id == id);
    }
    
    public static List<ResourceType> valueList() {
        return Arrays.asList(values());
    }
}
