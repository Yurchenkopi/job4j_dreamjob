package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.service.FileService;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileControllerTest {
    private FileService fileService;

    private FileController fileController;

    private FileDto testFile;

    @BeforeEach
    public void initServices() {
        fileService = mock(FileService.class);
        fileController = new FileController(fileService);
        testFile = new FileDto("testFile.img", new byte[] {1, 2, 3});
    }

    @Test
    public void whenRequestFileContentPageThenGetByteArrayPage() {
        var expectedResponseEntity = ResponseEntity.ok().body(testFile.getContent());
        when(fileService.getFileById(anyInt())).thenReturn(Optional.of(testFile));

        var view = fileController.getById(anyInt());

        assertThat(view).isEqualTo(expectedResponseEntity);
    }

    @Test
    public void whenRequestFileContentPageThenFileNotFound() {
        var expectedResponseEntity = ResponseEntity.notFound().build();
        when(fileService.getFileById(anyInt())).thenReturn(Optional.empty());

        var view = fileController.getById(anyInt());

        assertThat(view).isEqualTo(expectedResponseEntity);
    }
}
