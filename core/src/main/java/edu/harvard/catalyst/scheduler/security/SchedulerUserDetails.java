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
package edu.harvard.catalyst.scheduler.security;

import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.entity.UserSession;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Bill Simons
 * @date 5/2/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
public class SchedulerUserDetails implements UserDetails {
    private User user;
    private UserSession userSession;
    private List<GrantedAuthority> authorities;

    public SchedulerUserDetails(User user, UserSession userSession) {
        this.user = user;
        this.userSession = userSession;
        authorities = initAuthorities(user);

    }

    private List<GrantedAuthority> initAuthorities(User user) {
        ArrayList<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
        authList.add(user.getInstitutionRole().getType());
        return authList;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public User getUser() {
        return user;
    }

    public UserSession getUserSession() {
        return userSession;
    }

    public void setUserSession(UserSession userSession) {
        this.userSession = userSession;
    }

    public String getPassword() {
        return user.getPassword();
    }

    public String getUsername() {
        return user.getEcommonsId();
    }

    public boolean isAccountNonExpired() {
        return user.getActive();
    }

    public boolean isAccountNonLocked() {
        return user.getActive();
    }

    public boolean isCredentialsNonExpired() {
        return user.getActive();
    }

    public boolean isEnabled() {
        return user.getActive();
    }

    public String getSalt() {
        return user.getSalt();
    }
}
