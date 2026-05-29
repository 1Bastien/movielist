package com.efrei.movielist.app.client;

import com.efrei.movielist.app.dto.RegisterDto;
import com.efrei.movielist.app.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceClientTest {

    @Mock RestTemplate restTemplate;
    private AuthServiceClient authServiceClient;

    @BeforeEach
    void setUp() {
        authServiceClient = new AuthServiceClient(restTemplate);
        ReflectionTestUtils.setField(authServiceClient, "authServiceUrl", "http://localhost:9998");
    }

    @Test
    void register_success_callsAuthService() {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("alice");
        dto.setEmail("alice@test.com");
        dto.setPassword("pass");
        when(restTemplate.postForEntity(anyString(), eq(dto), eq(Void.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        assertThatNoException().isThrownBy(() -> authServiceClient.register(dto));
        verify(restTemplate).postForEntity(contains("/users/register"), eq(dto), eq(Void.class));
    }

    @Test
    void register_badRequest_throwsIllegalArgumentException() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> authServiceClient.register(new RegisterDto()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already used");
    }

    @Test
    void loadUserByUsername_existing_returnsUserDetails() {
        UserDto dto = new UserDto();
        dto.setUsername("alice");
        dto.setPassword("hashed");
        when(restTemplate.getForObject(anyString(), eq(UserDto.class))).thenReturn(dto);

        UserDetails result = authServiceClient.loadUserByUsername("alice");

        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getPassword()).isEqualTo("hashed");
    }

    @Test
    void loadUserByUsername_nullResponse_throwsUsernameNotFoundException() {
        when(restTemplate.getForObject(anyString(), eq(UserDto.class))).thenReturn(null);

        assertThatThrownBy(() -> authServiceClient.loadUserByUsername("alice"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(restTemplate.getForObject(anyString(), eq(UserDto.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found",
                        org.springframework.http.HttpHeaders.EMPTY, null, null));

        assertThatThrownBy(() -> authServiceClient.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_urlContainsUsername() {
        UserDto dto = new UserDto();
        dto.setUsername("alice");
        dto.setPassword("hashed");
        when(restTemplate.getForObject(anyString(), eq(UserDto.class))).thenReturn(dto);

        authServiceClient.loadUserByUsername("alice");

        verify(restTemplate).getForObject(contains("/users/alice"), eq(UserDto.class));
    }
}
