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

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.security.SchedulerUserDetails;
import edu.harvard.catalyst.scheduler.util.OneWayPasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

/**
 * @author Bill Simons
 * @date 5/2/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
public class SchedulerDbAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final AuthService authService;

    public SchedulerDbAuthenticationProvider(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void additionalAuthenticationChecks(final UserDetails userDetails, final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
        final SchedulerUserDetails user = (SchedulerUserDetails) userDetails;
        
        final String encodedPassword = encodePassword(usernamePasswordAuthenticationToken, user);

        if(!userDetails.getPassword().equals(encodedPassword)) {
            SchedulerRuntimeException.logAndThrow("Credentials don't match");
        }
    }
    
    @Override
    protected UserDetails retrieveUser(final String username, final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
        final WebAuthenticationDetails authenticationDetails = (WebAuthenticationDetails) usernamePasswordAuthenticationToken.getDetails();
        
        final SchedulerUserDetails userDetails = authService.authenticateUser(username, toPassword(usernamePasswordAuthenticationToken), authenticationDetails.getSessionId(), authenticationDetails.getRemoteAddress());

        if(userDetails == null) {
            SchedulerRuntimeException.logAndThrow("Unable to authenticate user");
        }
        
        return userDetails;
    }

    static String encodePassword(final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken, final SchedulerUserDetails user) {
        final OneWayPasswordEncoder encoder = OneWayPasswordEncoder.getInstance();
        
        final String password = toPassword(usernamePasswordAuthenticationToken);
        
        return encoder.encode(password, user.getSalt());
    }

    static String toPassword(final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
        return usernamePasswordAuthenticationToken.getCredentials().toString();
    }
}
