package webster.requestresponse.parsing;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class ParsersTest {

    @Test
    public void asInt() {
        Optional<Integer> notAnInt = Parsers.asInt.apply(Optional.of("notAnInt"));
        Assert.assertFalse(notAnInt.isPresent());
    }
}
