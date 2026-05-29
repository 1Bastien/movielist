package com.efrei.movielist.app.client;

import com.efrei.movielist.app.dto.RegisterDto;
import com.efrei.movielist.app.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AuthServiceClient implements UserDetailsService {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public AuthServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void register(RegisterDto dto) {
        try {
            restTemplate.postForEntity(authServiceUrl + "/users/register", dto, Void.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new IllegalArgumentException("Username or email already used");
            }
            throw e;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UserDto user = restTemplate.getForObject(authServiceUrl + "/users/" + username, UserDto.class);
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(List.of())
                    .build();
        } catch (HttpClientErrorException.NotFound e) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }
}
