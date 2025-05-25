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
/**
 * 
 */
package edu.harvard.catalyst.scheduler.util;

import java.sql.Types;

import org.hibernate.Hibernate;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.BooleanType;
import org.hibernate.type.StringType;

/**
 * @author clint
 * @date Jul 29, 2013
 *
 */
public final class SchedulerH2Dialect extends H2Dialect {
    public SchedulerH2Dialect() {
        super();
        
        //TODO: It would be /really/ nice to factor this out to some common place
        //referenced by this class and my SheculerDialect (the Mysql one).  However,
        //Dialect.registerHibernateType() is protected, not public, and so can't be
        //invoked by a factored-out static method.  The H2 and Mysql dialects need 
        //to extend their respective dialect classes, so a common base insn't 
        //possible either. :( 
        this.registerHibernateType(Types.LONGVARCHAR, StringType.INSTANCE.getName());
        this.registerHibernateType(Types.BOOLEAN, BooleanType.INSTANCE.getName());

        this.registerFunction("group_concat", new StandardSQLFunction("group_concat", StringType.INSTANCE));
    }
}
