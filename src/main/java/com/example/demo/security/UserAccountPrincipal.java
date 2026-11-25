package com.example.demo.security;

import com.example.demo.entity.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class UserAccountPrincipal implements UserDetails {
    private final Integer id;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserAccountPrincipal(UserAccount account) {
        this(account, null);
    }

    public UserAccountPrincipal(UserAccount account, String employeeType) {
        this.id = account.getId();
        this.username = account.getUsername();
        this.password = account.getPasswordHash();
        this.enabled = account.isEnabled();
        List<GrantedAuthority> granted = new ArrayList<>();
        granted.add(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()));
        if (employeeType != null && !employeeType.isBlank()) {
            granted.add(new SimpleGrantedAuthority("ROLE_" + employeeType.trim().toUpperCase(Locale.ROOT)));
        }
        this.authorities = List.copyOf(granted);
    }

    public Integer getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
