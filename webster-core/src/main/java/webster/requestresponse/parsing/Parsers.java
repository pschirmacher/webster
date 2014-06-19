package webster.requestresponse.parsing;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Parsers {

    public static final SafeParser<String, String> asTrimmed = String::trim;
    public static final SafeParser<String, Integer> asInt = asTrimmed.andAlso(Integer::valueOf);
    public static final SafeParser<String, Long> asLong = asTrimmed.andAlso(Long::valueOf);
    public static final SafeParser<String, Double> asDouble = asTrimmed.andAlso(Double::valueOf);
    public static final SafeParser<String, BigInteger> asBigInt = asTrimmed.andAlso(BigInteger::new);
    public static final SafeParser<String, BigDecimal> asBigDecimal = asTrimmed.andAlso(BigDecimal::new);
    public static final Function<Optional<String>, List<String>> asList =
            optionalString -> optionalString
                    .map(commaSeparatedList -> Arrays.asList(commaSeparatedList.split(",")).stream()
                            .map(String::trim)
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
    public static final FormParser asForm = new FormParser();
    public static final MultiValueFormParser asMultiValueForm = new MultiValueFormParser();
    public static final SafeParser<String, Instant> asInstant = asTrimmed.andAlso(Instant::parse);
    // TODO support not just rfc1123, see http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1
    public static final SafeParser<String, Instant> asHttpDate = asTrimmed.andAlso(s -> {
        try {
            return rfc1123().parse(s).toInstant();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    });

    public static <T> Function<Optional<T>, Boolean> asTrueIfEqualTo(T testee) {
        return optionalT -> optionalT.isPresent() && testee.equals(optionalT.get());
    }

    public static interface SafeParser<I, O> extends Function<Optional<I>, Optional<O>> {

        @Override
        default Optional<O> apply(Optional<I> input) {
            try {
                return input.isPresent() ? Optional.of(parse(input.get())) : Optional.empty();
            } catch (Exception e) {
                // yummy
                return Optional.empty();
            }
        }

        default <P> SafeParser<I, P> andAlso(Function<O, P> nextParseFunction) {
            return i -> nextParseFunction.apply(this.parse(i));
        }

        O parse(I input) throws Exception;
    }

    public static DateFormat rfc1123() {
        // DateTimeFormatter.RFC_1123_DATE_TIME threw exception for Instance.now()
        // https://people.apache.org/~prasad/AHC-javadocs/constant-values.html#org.apache.ahc.util.DateUtil.PATTERN_RFC1123
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format;
    }
}