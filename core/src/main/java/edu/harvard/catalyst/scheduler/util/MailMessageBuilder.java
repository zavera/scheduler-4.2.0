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

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import org.springframework.mail.SimpleMailMessage;

public final class MailMessageBuilder {
	private final SimpleMailMessage message = new SimpleMailMessage();


	public MailMessageBuilder to(String to){
		message.setTo(to);
		return this;
	
	}
	
	public MailMessageBuilder subject(String subject){
		message.setSubject(subject);
        return this;
	}
	
	public MailMessageBuilder cc(String ccVal){
		message.setCc(ccVal);
		return this;
	}

	public MailMessageBuilder bcc(String bccVal){
		message.setBcc(bccVal);
		return this;
	}

	public MailMessageBuilder text(String textVal){
		message.setText(textVal);
		return this;
	}

	public SimpleMailMessage build(){
		if(message.getTo()== null || message.getSubject() == null || message.getText() == null){

			SchedulerRuntimeException.logDontThrow("Null Values in one or more of the fields 'to','subject','text'");
		}
		return message;
	}
}
