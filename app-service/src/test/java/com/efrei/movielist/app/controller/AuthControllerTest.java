package com.efrei.movielist.app.controller;

import com.efrei.movielist.app.client.AuthServiceClient;
import com.efrei.movielist.app.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AuthServiceClient authServiceClient;

    @Test
    void loginPage_returnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("nextUrl", (Object) null));
    }

    @Test
    void loginPage_withNext_putsNextUrlInModel() throws Exception {
        mockMvc.perform(get("/login").param("next", "/search?q=matrix"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("nextUrl", "/search?q=matrix"));
    }

    @Test
    void registerPage_returnsRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    void register_success_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "alice")
                        .param("email", "alice@test.com")
                        .param("password", "pass")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(authServiceClient).register(any());
    }

    @Test
    void register_duplicate_returnsRegisterWithError() throws Exception {
        doThrow(new IllegalArgumentException("Username or email already used"))
                .when(authServiceClient).register(any());

        mockMvc.perform(post("/register")
                        .param("username", "alice")
                        .param("email", "alice@test.com")
                        .param("password", "pass")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("form"));
    }
}
