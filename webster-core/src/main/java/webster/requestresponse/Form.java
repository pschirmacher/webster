package webster.requestresponse;

import webster.util.Maps;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static webster.requestresponse.parsing.Parsers.asForm;

public class Form {

    private final Map<String, Field> fields;

    public Form() {
        this(Maps.<String, Field>newMap().build());
    }

    public Form(Map<String, Field> fields) {
        this.fields = fields;
    }

    public boolean hasError() {
        return fields.values().stream().anyMatch(Field::hasError);
    }

    public Map<String, Field> fields() {
        return Collections.unmodifiableMap(fields);
    }

    public Field field(String name) {
        return fields.get(name);
    }

    public Form with(String field, String value) {
        fields.put(field, new Field(Optional.ofNullable(value), false, Collections.emptyList()));
        return this;
    }

    public static class Field extends ValueSupplier<Optional<String>> {

        private final boolean hasError;
        private final List<String> errors;

        public Field(Optional<String> value, boolean hasError, List<String> errors) {
            super(value);
            this.hasError = hasError;
            this.errors = errors;
        }

        public boolean hasError() {
            return hasError;
        }

        public String error() {
            return errors.get(0);
        }

        public List<String> errors() {
            return errors;
        }

        public String text() {
            return value().orElse("");
        }
    }

    public static class Description {

        private final Map<String, Pattern> fieldNameToPattern;

        public Description(Map<String, Pattern> fieldNameToPattern) {
            this.fieldNameToPattern = fieldNameToPattern;
        }

        public Form empty() {
            return createForm(f2p -> Maps.entry(f2p.getKey(), new Field(Optional.empty(), false, Collections.emptyList())));
        }

        public Form parse(Request request) {
            Map<String, String> values = request.body().parse(asForm);
            return createForm(f2p -> {
                String fieldName = f2p.getKey();
                return Maps.entry(
                        fieldName,
                        fieldFrom(values.get(fieldName), fieldNameToPattern.get(fieldName)));
            });
        }

        private Form createForm(Function<Map.Entry<String, Pattern>, Map.Entry<String, Field>> fieldFactory) {
            return new Form(fieldNameToPattern.entrySet().stream()
                    .map(fieldFactory)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }

        private Field fieldFrom(String value, Pattern pattern) {
            if (value != null) {
                return pattern.matcher(value).matches()
                        ? new Field(Optional.of(value), false, Collections.emptyList())
                        : new Field(Optional.of(value), true, Arrays.asList("should match " + pattern.pattern()));
            } else {
                return new Field(Optional.empty(), true, Arrays.asList("missing"));
            }
        }
    }

    public static class DescriptionBuilder {

        private final Map<String, Pattern> fieldNameToPattern = Maps.<String, Pattern>newMap().build();

        public DescriptionBuilder withField(String name, String regex) {
            fieldNameToPattern.put(name, Pattern.compile(regex));
            return this;
        }

        public Description build() {
            return new Description(fieldNameToPattern);
        }
    }
}
