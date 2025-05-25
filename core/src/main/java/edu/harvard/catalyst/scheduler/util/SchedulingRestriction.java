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
package edu.harvard.catalyst.scheduler.util;

public class SchedulingRestriction {
    private static final SchedulingRestriction instance = new SchedulingRestriction();
    private final String schedulingRestriction =
            PropertyHelper.getProperties("scheduler.properties", getClass()).getProperty("schedulingRestriction");
    private final String lastMinuteIndicator =
            PropertyHelper.getProperties("scheduler.properties", getClass()).getProperty("lastMinuteIndicator");

    private SchedulingRestriction() {}

    public static SchedulingRestriction getInstance() {
        return instance;
    }

    public int getSchedulingRestriction() {
        return this.getRestriction(schedulingRestriction);
    }

    public int getLastMinuteIndicator() {
        return this.getRestriction(lastMinuteIndicator);
    }

    private int getRestriction(String restriction) {
        int restrictionInt;
        double restrictionDouble;
        if (restriction == null) {
            return 0;
        }
        try {
            restrictionInt = Integer.parseInt(restriction);
        }
        catch(NumberFormatException e) {
            try {
                restrictionDouble = Double.parseDouble(restriction);
            }
            catch (NumberFormatException n) {
                return 0;
            }
            restrictionInt = (int)restrictionDouble;
        }
        if (restrictionInt < 1) {
            return 0;
        }
        else if (restrictionInt > 99) {
            return 99;
        }
        else {
            return restrictionInt;
        }
    }
}
