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
package edu.harvard.catalyst.scheduler.entity.reporttemplate;


import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 10/21/14
 * Time: 1:47 PM
 */

public class Edge implements Comparable {

    public enum JoinType {
        INNER("inner join"),
        LEFT("left join"),
        RIGHT("right join");

        private String display;

        JoinType(String display) { this.display = display; }

        @Override
        public String toString() {
            return display;
        }
    }

    public enum JoinAssembly {
        leftField {
            String assembled(String lhs,
                             String rhs,
                             String joinField) {

                    return lhs + "." + joinField + " = " + rhs + ".id";
            }
        },
        rightField {
            String assembled(String lhs,
                             String rhs,
                             String joinField) {
                return rhs + "." + joinField + " = " + lhs + ".id";
            }
        },
        wholeField {
            String assembled(String lhs,
                             String rhs,
                             String joinField) {
                return joinField;
            }
        } ;

        abstract String assembled(String lhs,
                                  String rhs,
                                  String joinField);
    }

    private final Vertex pointOne;
    private final Vertex pointTwo;

    final private JoinType joinType;
    private String join;

    public Edge (Vertex pointOne,
                 Vertex pointTwo,
                 JoinType joinType,
                 String joinField) {
        this(pointOne, pointTwo, joinType, joinField, JoinAssembly.leftField);
    }
    public Edge(Vertex pointOne,
                Vertex pointTwo,
                JoinType joinType,
                String joinField,
                JoinAssembly joinAssembly) {
        this.pointOne = pointOne;
        this.pointTwo = pointTwo;

        this.joinType = joinType;

        setJoinFromCtor(joinField, pointOne, pointTwo, joinAssembly);
    }

    void setJoinFromCtor(String joinField,
                         Vertex pointOne,
                         Vertex pointTwo,
                         JoinAssembly joinAssembly) {
        String lhs = pointOne.getTableShortName();
        String rhs = pointTwo.getTableShortName();

        this.join = joinAssembly.assembled(lhs, rhs, joinField);
    }

    public Vertex getPointTwo() {
        return pointTwo;
    }

    public Vertex getPointOne() {
        return pointOne;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    void produceJoinLists(List<String> whereClauseList,
                          boolean previousLeftJoin,
                          Vertex previousRhs,
                          StringBuilder builder,
                          List<String> fromTables) {

        if (getJoinType().equals(JoinType.INNER)) {
            String tableName = pointTwo.fullName();
            fromTables.add(tableName);
            whereClauseList.add(getJoin());
        }
        else if (getJoinType().equals(JoinType.LEFT)) {
            if ( ! previousLeftJoin && previousRhs != null) {

                if (!builder.toString().contains(" INNER JOIN "+previousRhs.fullName())) {
                    //use previous rhs in string
                    builder .append(" INNER JOIN ")
                            .append(previousRhs.fullName());
                }
                whereClauseList.remove(previousRhs.fullName());
                fromTables.remove(previousRhs.fullName());
            }
            // part of string that starts 'left join...'
            builder
                    .append(" ")
                    .append(getJoinType())
                    .append(" ")
                    .append(getPointTwo().fullName())
                    .append(" on ")
                    .append(getJoin());
        }
        else {
            SchedulerRuntimeException.logAndThrow("Sorry, supporting only INNER and LEFT joins.");
        }
    }

    public String getJoin() {
        return join;
    }

    @Override
    public String toString() {
        return pointOne + " <- (" + joinType + ") " + join + " -> " + pointTwo;
    }

    @Override
    public int compareTo(Object obj) {
        // a bit shady if you incorrectly have two edges with same endpoints but different joins

        Edge other = (Edge)obj;

        if (other == null) {
            return 1;
        }

        Integer thisOne = getPointOne().getPriority();
        Integer otherOne = other.getPointOne().getPriority();

        int compare1 = thisOne.compareTo(otherOne);
        if (compare1 != 0) {
            return compare1;
        }

        thisOne = getPointTwo().getPriority();
        otherOne = other.getPointTwo().getPriority();

        return thisOne.compareTo(otherOne);
    }

}
