package com.skypro.simplebanking.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skypro.simplebanking.dto.BankingUserDetails;
import com.skypro.simplebanking.dto.CreateUserRequest;
import com.skypro.simplebanking.entity.User;
import com.skypro.simplebanking.repository.UserRepository;
import com.skypro.simplebanking.testData.TestData;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
//@Transactional
//@Sql("/sql/fill.sql")
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    TestData testData;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @Transactional
    void createUser(@Value("${app.security.admin-token}") String token) throws Exception {
        String username = "john";
        String requestBody = new JSONObject()
                .put("username", username)
                .put("password", "xxx")
                .toString();
        mockMvc.perform(post("/user/")
                        .header("X-SECURITY-ADMIN-KEY", token)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.accounts").isNotEmpty());
    }

    @Test
    @WithMockUser
    void getAllUsers() throws Exception {
        mockMvc.perform(get("/user/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
        //If user name and password are changed, it still works. Any a proper way to fix this?
        //Using Auth doesn't help
    void getMyProfile() throws Exception {
        BankingUserDetails authUser = testData.randomAuthUser();
        mockMvc.perform(get("/user/me")
                        .with(user(authUser)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.username").value(authUser.getUsername()),
                        jsonPath("$.id").value(authUser.getId()));
    }
}

