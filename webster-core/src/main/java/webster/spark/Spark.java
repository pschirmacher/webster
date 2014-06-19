package webster.spark;

import spark.utils.SparkUtils;

import java.util.*;

public class Spark {

    // taken from spark (https://github.com/perwendel/spark)
    public static Map<String, String> getParams(List<String> request, List<String> matched) {
        Map<String, String> params = new HashMap<>();

        for (int i = 0; (i < request.size()) && (i < matched.size()); i++) {
            String matchedPart = matched.get(i);
            if (SparkUtils.isParam(matchedPart)) {
                params.put(matchedPart.toLowerCase(), request.get(i));
            }
        }
        return Collections.unmodifiableMap(params);
    }

    // taken from spark (https://github.com/perwendel/spark)
    public static List<String> getSplat(List<String> request, List<String> matched) {
        int nbrOfRequestParts = request.size();
        int nbrOfMatchedParts = matched.size();

        boolean sameLength = (nbrOfRequestParts == nbrOfMatchedParts);

        List<String> splat = new ArrayList<>();

        for (int i = 0; (i < nbrOfRequestParts) && (i < nbrOfMatchedParts); i++) {
            String matchedPart = matched.get(i);

            if (SparkUtils.isSplat(matchedPart)) {

                StringBuilder splatParam = new StringBuilder(request.get(i));
                if (!sameLength && (i == (nbrOfMatchedParts - 1))) {
                    for (int j = i + 1; j < nbrOfRequestParts; j++) {
                        splatParam.append("/");
                        splatParam.append(request.get(j));
                    }
                }
                splat.add(splatParam.toString());
            }
        }
        return Collections.unmodifiableList(splat);
    }
}
