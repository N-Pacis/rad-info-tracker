package rw.auca.radinfotracker.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import rw.auca.radinfotracker.model.File;
import rw.auca.radinfotracker.model.enums.EFileSizeType;
import rw.auca.radinfotracker.model.enums.EFileStatus;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FileRepositoryTest {

    @Autowired
    private FileRepository fileRepository;

    @AfterEach
    void tearDown() {
        fileRepository.deleteAll();
    }

    @Test
    void itShouldFindFileByName(){
        String fileName = "Testing file Name";

        File file = new File(fileName, "","",0, EFileSizeType.B, "", EFileStatus.SAVED);
        fileRepository.save(file);

        Optional<File> expected = fileRepository.findByName(fileName);

        assertThat(expected.isPresent()).isTrue();
        assertEquals(expected.get().getName(), fileName);
    }

    @Test
    void itShouldReturnNullWhenFileByNameDoesNotExist(){
        String fileName = "Testing file Name";
        Optional<File> expected = fileRepository.findByName(fileName);

        assertThat(expected.isPresent()).isFalse();
    }
}