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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.harvard.catalyst.scheduler.dto.UserDTO;
import edu.harvard.catalyst.scheduler.dto.SubjectsDTO;
import edu.harvard.catalyst.scheduler.entity.Gender;
import edu.harvard.catalyst.scheduler.entity.Subject;
import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * @author Anusha Chitla
 *
 */
public class ServiceTestsDTOUtility {

	public static UserDTO createUserDTO(){

		UserDTO dto = new UserDTO(); 
		dto.setId(2);
		dto.setPassword("Adr12$");
		dto.setEcommonsId("br703");
		dto.setFirstName("Ben");
		dto.setMiddleName("abc");
		dto.setLastName("Randall");
		dto.setPrimaryPhone("9871235360");
		dto.setSecondaryPhone("1237643980");
		dto.setEmail("brandall@bidmc.org"); 
		dto.setFax("9723456714");
		dto.setPager("54328");
		dto.setDivision(1);
		dto.setDepartment(2);
		dto.setRoleId(4); 
		dto.setInstitutionId(6);
		dto.setCredential(4);
		dto.setFacultyRank(3);
		dto.setActive(false);
		dto.setAuthStatus(1);

		return dto;

	}

	public static List<User> getAllUsers() throws Exception{
		List<User> users = new ArrayList<User>();
		User user = new User();
		User user1 = new User();
		User user2 = new User();

		user.setFirstName("brooke");
		user.setLastName("jones");
		user.setEcommonsId("bj367");
		user1.setFirstName("felicia");
		user1.setLastName("green");
		user1.setEcommonsId("fg567");
		user2.setFirstName("Steph");
		user2.setLastName("green");
		user2.setEcommonsId("sg736");

		users.add(user);
		users.add(user1);
		users.add(user2);
		return users;
	}

	public static List<User> filteredUsersByLastName() throws Exception{
		List<User> filteredUsers = new ArrayList<User>();
		User user1 = new User();
		User user2 = new User();

		user1.setFirstName("felicia");
		user1.setLastName("green");
		user1.setEcommonsId("fg567");
		user1.setPrimaryPhone("9724561289");
		user2.setFirstName("Steph");
		user2.setLastName("green");
		user2.setEcommonsId("sg736");
		user2.setPrimaryPhone("97244567");

		filteredUsers.add(user1);
		filteredUsers.add(user2);
		return filteredUsers;
	}

	public static List<User> filteredUsersByEcommons() throws Exception{

		List<User> users = new ArrayList<User>();
		User user1 = new User();
		user1.setFirstName("brooke");
		user1.setLastName("jones");
		user1.setEcommonsId("bj367");
		user1.setPrimaryPhone("97244320");
		users.add(user1);
		return users;

	}

	public static User loginCredentials() throws Exception{

		User user = new User();
		user.setEcommonsId("tk468");
		user.setId(1);
		user.setPassword("abc");
		user.setEmail("tk123@gmail.com");
		user.setActive(true);
		return user;
	}

	public static UserDTO toDeactivate() throws Exception{

		UserDTO user = new UserDTO();
		user.setFirstName("carol");
		user.setLastName("turner");
		user.setEcommonsId("ct968");
		user.setPassword("GTui892%%");
		user.setEmail("ct8923@gmail.com");
		user.setUserToDeactivateId(5);
		return user;
	}

	public static UserDTO toActivate() throws Exception{

		UserDTO user = new UserDTO();
		user.setFirstName("Harry");
		user.setLastName("Levis");
		user.setEcommonsId("hl468");
		user.setPassword("HLui892%%");
		user.setEmail("hl54923@gmail.com");
		user.setUserToActivateId(7);
		return user;
	}

	public static UserDTO updatePassword()throws Exception{

		UserDTO user = new UserDTO();
		user.setFirstName("carol");
		user.setLastName("turner");
		user.setEcommonsId("ct968");
		user.setEmail("ct8923@gmail.com");
		user.setErrorMsg("Insufficient Password");
		return user;
	}

	//NOTE:  if these values are changed, tests WILL FAIL
	public static SubjectsDTO createSubject(boolean shouldSetId) throws Exception{
		
		SubjectsDTO s = new SubjectsDTO();
		if (shouldSetId) {
			s.setId(1);
		}
		s.setActive(false);
		s.setAuthenticated(true);

		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		Date dob = sdf.parse("05/05/1977");

		s.setBirthdate(dob);
		s.setCity("Cambridge");
		s.setCountry(1);
		s.setCreatedDate(new Date());
		s.setEthnicity(2);
		s.setFirstName("Joy");
		s.setGender(2);
		s.setRace(1);
		s.setZip("02139");
		s.setPrimaryContactNumber("1231231233");
		s.setLastName("Hall");
		s.setStreetAddress1("120 Liberty Plaza");
		s.setState(3);

		return s;
	}


	public static void setUpSubjectDao(int idToSet, SubjectDAO mockSubjectDAO, boolean doEncryption){

		// http://stackoverflow.com/questions/28341442/mock-void-methods-which-change-their-argument
		// use doAnswer() to go inside a void method, mock it, and alter the arguments
		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();

				// args[0]: the first arg, that I want to modify
				Subject subject = (Subject)args[0];
				subject.setId(idToSet);
				if (doEncryption) {
					SubjectDataEncryptor.encryptSubjectInPlace(subject);
				}
				//return null since it's a void method:
				return null;
			}}).when(mockSubjectDAO).createSubject(any(Subject.class));

		when(mockSubjectDAO.mrnInfoExists(anyObject())).thenReturn(false);

		Gender female = new Gender();
		female.setName("Female");
		female.setCode("F");
		female.setId(2);

		when(mockSubjectDAO.findByGenderId(anyInt())).thenReturn(female);
	}


}
