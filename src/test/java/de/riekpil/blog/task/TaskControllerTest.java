package de.riekpil.blog.task;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    public void shouldRejectCreatingReviewsWhenUserIsAnonymous() throws Exception {
        mockMvc.perform(
                        post("/api/tasks/demo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"taskTitle\": \"Learn MockMvc \"}")
                                .with(csrf())
                )
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser("CGE")
    public void shouldRejectCreatingReviewsWhenUserIsOk() throws Exception {
        /**
         * Du Mock d'un service injecté en mémoire dans le controller DemoTaskController
         * je valorise le comportement de l'appel de la méthode createTask du service taskService
         */
        when(taskService.createTask(anyString())).thenReturn(82L);

        this.mockMvc
                .perform(
                        post("/api/tasks/demo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"taskTitle\": \"Learn MockMvc\"}")
                                .with(csrf())
                )
                .andExpect(status().isCreated()) //Si c'est ok created : 201 car c'est ce que retourne notre controller
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", Matchers.containsString("82")));


    }

    /**
     * test de la suppression
     * On doit être identifié :@WithMockUser("duke") mais aussi avoir le rôle ADMIN :
     *
     * @throws Exception
     * @DeleteMapping
     * @RolesAllowed("ADMIN")
     * @RequestMapping("/{taskId}")
     */
    @Test
    @WithMockUser("toto")
    public void shouldRejectDeletingReviewsWhenUserLacksAdminRole() throws Exception {
        this.mockMvc
                .perform(delete("/api/tasks/demo/42")) //Appel delete http : API REST
                .andExpect(status().isForbidden());
        //Normalement on doit être refusé : 403 car on n'a pas positionné le rôle
    }

    @Test
    public void shouldAllowDeletingReviewsWhenUserIsAdmin() throws Exception {
        this.mockMvc
                .perform(
                        delete("/api/tasks/demo/42") //Suppression de la tâche 42
                                .with(
                                        SecurityMockMvcRequestPostProcessors.
                                                user("gigi").roles("ADMIN"))
                                //le user et ses rôles ne pas oublier @EnableGlobalMethodSecurity(jsr250Enabled = true)
                                .with(csrf())
                )
                .andExpect(status().isOk());//Status : 200

        verify(taskService).deleteTask(42L);
        //Vérification la suppression c'est bien effectuée avec 42
    }
}
