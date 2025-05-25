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
package edu.harvard.catalyst.scheduler.service;

import static java.util.function.Function.identity;

import java.util.Optional;
import java.util.function.Function;

import edu.harvard.catalyst.scheduler.entity.HasName;

public interface ServiceHelpers {


	String TO = " to ";
	String NULL = ": NULL ";
	String COLON = ": ";
	String SPACE = " ";
	String COMMA = ", ";

	default String lookupFieldById(final Integer id, final Function<Integer, HasName> lookupField) {
		if (id != 0) {
			return lookupField.apply(id).getName();
		} 
		
		return null;
	}
	
	default <F> Optional<String> makeFieldString(final String fieldName, final F dtoField, final F userField) {
		return makeFieldString(fieldName, dtoField, userField, identity(), identity());
	}

	default <F> Optional<String> makeFieldString(final String fieldName, final F dtoField, final F userField, final Function<F, F> mungeUserField, final Function<F, F> mungeDtoField) {
		if (userField != null && dtoField != null && !dtoField.equals(userField)) {
			return Optional.of(SPACE + fieldName + COLON + mungeUserField.apply(userField) + TO + mungeDtoField.apply(dtoField) + COMMA);
		}
		else if (userField == null && dtoField != null) {
			return Optional.of(SPACE + fieldName + NULL + TO +  mungeDtoField.apply(dtoField) + COMMA);
		} else if (null == dtoField && userField != null){
			return Optional.of(SPACE + fieldName + COLON + mungeUserField.apply(userField) + TO + NULL + COMMA);
		}
		
		return Optional.empty();
	}

	/**make a field string, but do not munge or encrypt the original or new values */
	default <C, F extends HasName> Optional<String> makeFieldString(final String fieldName, final String mappedTo, final F userField, C id, final Function<F, C> getUserFieldToCompareTo) {
		return makeFieldString(fieldName, mappedTo, userField, id, getUserFieldToCompareTo, identity(), identity());
	}

	/**
	 * @param mappedTo The new value
	 * @param userField the original value
	 * @param id the id of the original value
	 * @param getUserFieldToCompareTo the function to retrieve the id for comparison
	 * @param mungeMappedTo the function to 'munge' or alter/transform the mappedTo/new value
	 * @param mungeUserField the function to 'munge' or alter/transform the userField/original value
	 *
	 * @return an Optional String, that contains the name of the field, followed by the original value; then the new value.
	 * if the field is encrypted, then the fields are encrypted first before being inserted into the string.
	 *
	 * Note: Null values are not 'munged'
	 * */
	default <C, F extends HasName> Optional<String> makeFieldString(final String fieldName, final String mappedTo, final F userField, final C id, final Function<F, C> getUserFieldToCompareTo, final Function<String, String> mungeUserField, final Function<String, String> mungeMappedTo) {
		if (userField != null) {
			final C userFieldToCompareTo = getUserFieldToCompareTo.apply(userField);
			
			if (!id.equals(userFieldToCompareTo) && mappedTo != null) {
				return Optional.of(SPACE + fieldName + COLON + mungeUserField.apply(userField.getName()) + TO + mungeMappedTo.apply(mappedTo) + COMMA);
			} else  if (null == mappedTo) {
				return Optional.of(SPACE + fieldName + COLON + mungeUserField.apply(userField.getName()) + TO + NULL + COMMA);
			} else {
				return Optional.empty();
			}
		} else {
			if(mappedTo != null)
			{
				return Optional.of(SPACE + fieldName + NULL + TO + mungeMappedTo.apply(mappedTo) + COMMA);
			}
			else {
				//both null
				return Optional.empty();
			}
		}
	}
}
