package fr.ccouturi;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestResult {

    @Test
    public void shouldConverttoJson() throws JsonProcessingException {
        Result result = new Result("my_product", Boolean.TRUE, "http://test.fr");
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("{\"product\":\"my_product\",\"status\":true,\"urls\":[\"http://test.fr\"]}", mapper.writeValueAsString(result));
    }
}
