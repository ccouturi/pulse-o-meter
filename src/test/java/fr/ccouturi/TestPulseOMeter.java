package fr.ccouturi;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TestPulseOMeter {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_ifConfigFileNotFound() throws IOException {
        new PulseOMeter((File) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_ifConfigFileIsNotValid() throws IOException {
        new PulseOMeter(loadFile("invalid_config_file.json"));
    }

    public void shouldApplyConfig() throws IOException {
        new PulseOMeter(loadFile("valid_config_file.json"));
        // TODO test content
    }

    private File loadFile(String fileNameForClassLoader) {
        return new File(Thread.currentThread().getContextClassLoader()//
                .getResource(fileNameForClassLoader).getFile());
    }
}
