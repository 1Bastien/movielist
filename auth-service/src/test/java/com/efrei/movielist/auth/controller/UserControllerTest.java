package com.efrei.movielist.auth.controller;

import com.efrei.movielist.auth.config.SecurityConfig;
import com.efrei.movielist.auth.dto.UserDto;
import com.efrei.movielist.auth.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean UserService userService;

    @Test
    void register_newUser_returns201() throws Exception {
        UserDto dto = new UserDto();
        dto.setUsername("alice");
        when(userService.register(any(), any(), any())).thenReturn(dto);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "alice", "email", "alice@test.com", "password", "pass"))))
                .andExpect(status().isCreated());
    }

    @Test
    void register_duplicateUser_returns400() throws Exception {
        when(userService.register(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Username already taken"));

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "alice", "email", "alice@test.com", "password", "pass"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUser_existing_returns200WithDto() throws Exception {
        UserDto dto = new UserDto();
        dto.setUsername("alice");
        dto.setEmail("alice@test.com");
        dto.setPassword("hashed");
        when(userService.findByUsername("alice")).thenReturn(dto);

        mockMvc.perform(get("/users/alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.password").value("hashed"));
    }

    @Test
    void getUser_notFound_returns404() throws Exception {
        when(userService.findByUsername("unknown"))
                .thenThrow(new UsernameNotFoundException("not found"));

        mockMvc.perform(get("/users/unknown"))
                .andExpect(status().isNotFound());
    }
}
