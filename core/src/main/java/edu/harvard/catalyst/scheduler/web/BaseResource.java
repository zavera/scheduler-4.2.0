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
package edu.harvard.catalyst.scheduler.web;

import com.google.gson.*;
import edu.harvard.catalyst.scheduler.entity.BookedVisit;
import edu.harvard.catalyst.scheduler.entity.SubjectMrn;
import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.entity.UserSession;
import edu.harvard.catalyst.scheduler.security.SchedulerSession;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.Date;

/**
 * @author Bill Simons
 * @date 5/21/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
@Produces(MediaType.TEXT_PLAIN) //TODO can this be application/json?
public class BaseResource {
	protected final Gson gson;

	public final Gson bookedVisitSkippingGson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {

		@Override
		public boolean shouldSkipClass(final Class<?> clazz) {
			return clazz == BookedVisit.class;
		}

		/**
		 * Custom field exclusion goes here
		 */
		@Override
		public boolean shouldSkipField(final FieldAttributes f) {
			return false;
		}
	}).serializeNulls().create();

	public final Gson subjectMrnSkippingGson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {

		@Override
		public boolean shouldSkipClass(final Class<?> clazz) {
			return clazz == SubjectMrn.class;
		}

		/**
		 * Custom field exclusion goes here
		 */
		@Override
		public boolean shouldSkipField(final FieldAttributes f) {
			return false;
		}

	}).serializeNulls().create();

	@Context
	protected HttpServletRequest request;

	@Autowired
	private SchedulerSession session;

	// enable injection by unit-tests
	void setSession(final SchedulerSession session) {
		this.session = session;
	}
	
	void setRequest(final HttpServletRequest request) {
		this.request = request;
	}

	public BaseResource() {
		final GsonBuilder builder = new GsonBuilder();

		// This is for testing so we can generate json strings with longs for dates from a serialized DTO
		// TODO-XH : verify that this does not break something else!
		builder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(final Date date, final Type typeOfT, final JsonSerializationContext context) {
				// the key here is that we serialize a date to its value in milliseconds (a long int)
				// This way later when deserializing, the Date handler, which expects a long, will be able to
				// re-construct the date
				return new JsonPrimitive(date.getTime());
			}
		});

		// Register an adapter to manage the date types as long values
		builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
				return new Date(json.getAsJsonPrimitive().getAsLong());
			}
		});

		gson = builder.create();
	}

	protected User getUser() {
		return session.getUserDetails().getUser();
	}

	protected UserSession getUserSession() {
		return session.getUserDetails().getUserSession();
	}

	protected void updateUserSession(final UserSession us) {
		session.getUserDetails().setUserSession(us);
	}

	protected String getRemoteHost() {
		return request.getRemoteHost();
	}

	protected String getServerName() {
		return request.getServerName();
	}

	protected int getServerPort() {
		return request.getServerPort();
	}

	protected String getContextPath() {
		return request.getContextPath();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Exclude {
	}
}
