package ru.job4j.dreamjob.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    private UserService userService;

    private UserController userController;

    @BeforeEach
    public void initServices() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    public void whenRequestUserRegistrationPageThenGetRegistrationPage() {
        var model = new ConcurrentModel();
        var httpSession = mock(HttpSession.class);

        var view = userController.getRegistrationPage(model, httpSession);

        assertThat(view).isEqualTo("users/register");
    }

    @Test
    public void whenRequestLoginPageThenGetLoginPage() {
        var model = new ConcurrentModel();
        var httpSession = mock(HttpSession.class);

        var view = userController.getLoginPage(model, httpSession);

        assertThat(view).isEqualTo("users/login");
    }

    @Test
    public void whenPostRegistrationUserInfoThenSameDataAndRedirect() {
        var user = new User(1, "ya@ya.ru", "ivan", "12345");
        var userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        when(userService.save(userArgumentCaptor.capture())).thenReturn(Optional.of(user));

        var model = new ConcurrentModel();
        var view = userController.register(user, model);
        var actualUser = userArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualUser).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    public void whenPostRegistrationUserInfoThenRegistrationError() {
        var expectedMessage = "Пользователь с таким email уже зарегистрирован";

        when(userService.save(any())).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.register(new User(), model);
        var actualMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void whenLogoutThenRedirect() {
        var httpSession = mock(HttpSession.class);

        var view = userController.logout(httpSession);

        assertThat(view).isEqualTo("redirect:/users/login");
    }

    @Test
    public void whenLoginUserThenAccessIsApprovedAndRedirect() {
        var user = new User(1, "ya@ya.ru", "ivan", "12345");
        var emailArgumentCaptor = ArgumentCaptor.forClass(String.class);
        var passwordArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(userService.findByEmailAndPassword(
                emailArgumentCaptor.capture(),
                passwordArgumentCaptor.capture()
        )).thenReturn(Optional.of(user));


        var model = new ConcurrentModel();
        var httpServletRequest = mock(HttpServletRequest.class);

        when(httpServletRequest.getSession()).thenReturn(new MockHttpSession());

        var view = userController.loginUser(user, model, httpServletRequest);
        var actualEmail = emailArgumentCaptor.getValue();
        var actualPassword = passwordArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualEmail).isEqualTo(user.getEmail());
        assertThat(actualPassword).isEqualTo(user.getPassword());
    }

    @Test
    public void whenLoginUserThenLoginErrorMessage() {
        var expectedErrorMessage = "Почта или пароль введены неверно";

        when(userService.findByEmailAndPassword(any(), any())).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var httpServletRequest = mock(HttpServletRequest.class);
        var view = userController.loginUser(new User(), model, httpServletRequest);
        var actualErrorMessage = model.getAttribute("error");

        assertThat(view).isEqualTo("users/login");
        assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);
    }

}
