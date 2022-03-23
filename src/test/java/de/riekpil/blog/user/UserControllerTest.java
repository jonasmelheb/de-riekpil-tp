package de.riekpil.blog.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserNotFoundException userNotFoundException;

    @Test
    public void shouldRejectAccessToUserWhenUserIsNotAdmin() throws Exception {
        this.mockMvc
                .perform(get("/api/users/younes").with(
                        SecurityMockMvcRequestPostProcessors.
                                user("toto").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser("toto")
    public void shouldReturnUserNotFound() throws Exception {
        when(userService.getUserByUsername("younes"))
                .thenThrow(new UserNotFoundException("Can't find this user *sad smiley*"));
        this.mockMvc
                .perform(get("/api/users/younes"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotAllowCreatingUserWhenUserIsNotAdmin() throws Exception {
        User user = new User("younes", "younes@email.com");
        String body = (new ObjectMapper()).valueToTree(user).toString();
        this.mockMvc
                .perform(
                        post("/api/users").with(
                                        SecurityMockMvcRequestPostProcessors.
                                                user("toto").roles("USER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                                .with(csrf())
                )
                .andExpect(status().isCreated());

    }

    @Test
    public void shouldAllowCreatingUserWhenUserIsAdmin() throws Exception {
        User user = new User("younes", "younes@email.com");
        String body = (new ObjectMapper()).valueToTree(user).toString();
        this.mockMvc
                .perform(
                        post("/api/users").with(
                                        SecurityMockMvcRequestPostProcessors.
                                                user("toto").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                                .with(csrf())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldAllowDeletingUserWhenUserIsAdmin() throws Exception {
        this.mockMvc
                .perform(
                        delete("/api/users/younes")
                                .with(
                                        SecurityMockMvcRequestPostProcessors.
                                                user("admin").roles("ADMIN"))
                                .with(csrf())
                )
                .andExpect(status().isOk());

    }
}
