package webster.requestresponse;

public final class Processors {
    public static ResponseBodyProcessor<String> stringProcessor(final int maxLen){
        return new ResponseBodyProcessor<String>() {
            @Override
            public String process(StringResponseBody body) {
                return body.content().length() > maxLen ? body.content().substring(0, maxLen) + "..." : body.content();
            }

            @Override
            public String process(InputStreamResponseBody body) {
                return body.toString();
            }

            @Override
            public String process(EmptyResponseBody body) {
                return "EMPTY";
            }
        };
    }
}
