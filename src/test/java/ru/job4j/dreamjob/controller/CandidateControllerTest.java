package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class CandidateControllerTest {
    private CandidateService candidateService;

    private CityService cityService;

    private CandidateController candidateController;

    private MultipartFile testFile;

    @BeforeEach
    public void initServices() {
        candidateService = mock(CandidateService.class);
        cityService = mock(CityService.class);
        candidateController = new CandidateController(candidateService, cityService);
        testFile = new MockMultipartFile("testFile.img", new byte[] {1, 2, 3});
    }

    @Test
    public void whenRequestCandidatesListPageThenGetPageWithCandidates() {
        var candidate1 = new Candidate(1, "test1", "desc1", now(), 1, 2);
        var candidate2 = new Candidate(2, "test2", "desc2", now(), 3, 4);
        var expectedCandidates = List.of(candidate1, candidate2);
        when(candidateService.findAll()).thenReturn(expectedCandidates);

        var model = new ConcurrentModel();
        var httpSession = mock(HttpSession.class);
        var view = candidateController.getAll(model, httpSession);
        var actualCandidates = model.getAttribute("candidates");

        assertThat(view).isEqualTo("candidates/list");
        assertThat(actualCandidates).isEqualTo(expectedCandidates);
    }

    @Test
    public void whenRequestCandidateCreationPageThenGetPageWithCities() {
        var city1 = new City(1, "Москва");
        var city2 = new City(2, "Санкт-Петербург");
        var expectedCities = List.of(city1, city2);
        when(cityService.findAll()).thenReturn(expectedCities);

        var model = new ConcurrentModel();
        var httpSession = mock(HttpSession.class);
        var view = candidateController.getCreationPage(model, httpSession);
        var actualCandidates = model.getAttribute("cities");

        assertThat(view).isEqualTo("candidates/create");
        assertThat(actualCandidates).isEqualTo(expectedCities);
    }

    @Test
    public void whenPostCandidateWithFileThenSameDataAndRedirectToCandidatesPage() throws Exception {
        var candidate = new Candidate(1, "test1", "desc1", now(), 1, 5);
        var fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.save(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(candidate);

        var model = new ConcurrentModel();
        var view = candidateController.create(candidate, testFile, model);
        var actualCandidate = candidateArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).usingRecursiveComparison().isEqualTo(candidate);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);

    }

    @Test
    public void whenSomeExceptionThrownThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException("Failed to write file");
        when(candidateService.save(any(), any())).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = candidateController.create(new Candidate(), testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenRequestCandidateIdPageThenGetPageWithCandidateId() {
        var city1 = new City(1, "Москва");
        var city2 = new City(2, "Санкт-Петербург");
        var expectedCities = List.of(city1, city2);

        var expectedCandidate = Optional.of(new Candidate(1, "test1", "desc1", now(), 1, 2));
        var expectedCandidateId = expectedCandidate.get().getId();

        when(cityService.findAll()).thenReturn(expectedCities);
        when(candidateService.findById(1)).thenReturn(expectedCandidate);

        var model = new ConcurrentModel();
        var httpSession = mock(HttpSession.class);
        var view = candidateController.getById(model, expectedCandidateId, httpSession);
        var actualCities = model.getAttribute("cities");
        var actualCandidate = model.getAttribute("candidate");

        assertThat(view).isEqualTo("candidates/one");
        assertThat(actualCities).isEqualTo(expectedCities);
        assertThat(actualCandidate).isEqualTo(expectedCandidate.get());
    }

    @Test
    public void whenRequestCandidateIdPageThenNoCandidatesFound() {
        Optional<Candidate> expectedCandidate = Optional.empty();
        when(candidateService.findById(anyInt())).thenReturn(expectedCandidate);

        var model = new ConcurrentModel();
        var httpSession = mock(HttpSession.class);
        var view = candidateController.getById(model, anyInt(), httpSession);
        var actualCandidateMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualCandidateMessage).isEqualTo("Резюме с указанным идентификатором не найдено");
    }

    @Test
    public void whenUpdateCandidateThenUpdatedDataAndRedirectToCandidatesPage() throws Exception {
        var candidate = new Candidate(1, "test1", "desc1", now(), 1, 5);
        var fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.update(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);
        var actualCandidate = candidateArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).usingRecursiveComparison().isEqualTo(candidate);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);

    }

    @Test
    public void whenUpdateCandidateThenUpdatedError() {
        var candidate = new Candidate(1, "test1", "desc1", now(), 1, 5);
        when(candidateService.update(any(Candidate.class), any(FileDto.class))).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);
        var actualCandidateMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualCandidateMessage).isEqualTo("Резюме с указанным идентификатором не найдено");
    }

    @Test
    public void whenSomeExceptionThrownWhileUpdateThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException("Failed to update file");
        when(candidateService.update(any(), any())).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = candidateController.update(new Candidate(), testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenDeleteCandidateThenSuccessfullyDeleteAndRedirect() {
        when(candidateService.deleteById(anyInt())).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.delete(model, anyInt());

        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @Test
    public void whenDeleteCandidateThenUnsuccessfullyDelete() {
        when(candidateService.deleteById(anyInt())).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.delete(model, anyInt());
        var actualCandidateMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualCandidateMessage).isEqualTo("Резюме с указанным идентификатором не найдено");
    }

}
