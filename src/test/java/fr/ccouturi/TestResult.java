package fr.ccouturi;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;

public class TestResult {

    @Test
    public void shouldConverttoJson() throws JsonProcessingException {
        Date now = new Date(123456L);
        Result result = new Result("my_product", "PASS", Boolean.TRUE, "1.4", now, "http://test.fr");
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("{\"product\":\"my_product\",\"status\":true,\"urls\":[\"http://test.fr\"],\"content\":\"PASS\",\"version\":\"1.4\",\"pduiVersion\":null,\"check_date\":123456}", mapper.writeValueAsString(result));
    }
}
