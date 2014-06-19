package webster;

import org.fusesource.scalate.japi.TemplateEngineFacade;
import org.fusesource.scalate.layout.DefaultLayoutStrategy;
import scala.collection.mutable.ArraySeq;

public class Scalate {

    public static TemplateEngineFacade layoutTemplateEngine() {
        TemplateEngineFacade engineFacade = new TemplateEngineFacade();
        DefaultLayoutStrategy layoutStrategy = new DefaultLayoutStrategy(engineFacade.getEngine(), new ArraySeq<>(0));
        engineFacade.getEngine().layoutStrategy_$eq(layoutStrategy);
        return engineFacade;
    }
}
