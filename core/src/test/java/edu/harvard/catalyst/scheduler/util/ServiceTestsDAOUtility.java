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

import java.util.Date;

import com.google.common.collect.Sets;
import edu.harvard.catalyst.scheduler.entity.*;
import org.springframework.mail.SimpleMailMessage;

/**
 * @author Anusha Chitla
 *
 */
public class ServiceTestsDAOUtility {

	public static User createMockUser() {
		User user = new User();
		user.setPreviousLoginTime(null);
		user.setFirstName("Jane");
		user.setLastName("Swingler");
		user.setPassword("Jtc7101%");
		user.setEmail("js123@gmail.com");
		user.setId(1);
		user.setActive(true);
		user.setSalt("$3@Nn3DFT2012");
		InstitutionRole institutionRole = new InstitutionRole();
		institutionRole.setType(InstitutionRoleType.ROLE_SUPER_ADMIN);
		user.setInstitutionRole(institutionRole);
		return user;

	}

	public static User sampleUser(){
		User user = new User();
		user.setFirstName("carol");
		user.setLastName("turner");
		user.setEcommonsId("ct968");
		user.setPassword("GTui892%%");
		user.setEmail("ct8923@gmail.com");
		user.setId(5);
		user.setActive(true);
		return user;
	}

	public static UserSession createUserSession(User user) {
		UserSession userSession = new UserSession();
		userSession.setUser(user);
		userSession.setIpAddress("0:0:0:0:0:0:0:1");
		userSession.setLastAccessTime(new Date());
		return userSession;
	}

	public static SimpleMailMessage messageMock(){
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo("scheduler@gmail.com");
		message.setCc("cc");
		message.setBcc("bcc");
		message.setText("Email notifications");
		message.setFrom("scheduling@harvard.catalyst.edu");
		return message;
	}


	public static SublocationClosureInterval createSublocationClosureInterval() {
		SublocationClosureInterval interval = new SublocationClosureInterval();
		interval.setStartTime(new Date());
		interval.setEndTime(new Date());
		interval.setReason("Christmas Holiday");
		Sublocation sublocation = new Sublocation();
		sublocation.setId(1);
		interval.setSublocation(sublocation);
		return interval;
	}
	
	public static Subject sampleSubject(){
		Subject subject = new Subject();
		subject.setId(3);
		subject.setFirstName("Sara");
		subject.setLastName("Boro");
        subject.setSubjectMrnSet(Sets.newHashSet(new SubjectMrn(subject, "M002", "", null, null)));
        return subject;
		
	}
}


