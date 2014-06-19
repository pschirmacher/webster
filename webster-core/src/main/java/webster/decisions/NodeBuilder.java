package webster.decisions;

import webster.requestresponse.Request;
import webster.requestresponse.Response;
import webster.resource.Resource;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class NodeBuilder {

    public static DecisionOnTrueBuilder decide(BiFunction<Resource, Request, CompletableFuture<Boolean>> decision) {
        return new DecisionOnTrueBuilder("nameless", decision);
    }

    public static DecisionOnTrueBuilder decide(String name, BiFunction<Resource, Request, CompletableFuture<Boolean>> decision) {
        return new DecisionOnTrueBuilder(name, decision);
    }

    public static ActionAndThenBuilder act(BiFunction<Resource, Request, CompletableFuture<Void>> action) {
        return new ActionAndThenBuilder("nameless", action);
    }

    public static ActionAndThenBuilder act(String name, BiFunction<Resource, Request, CompletableFuture<Void>> action) {
        return new ActionAndThenBuilder(name, action);
    }

    public static Completion complete(BiFunction<Resource, Request, CompletableFuture<Response>> completion) {
        return new Completion("nameless", completion);
    }

    public static Completion complete(String name, BiFunction<Resource, Request, CompletableFuture<Response>> completion) {
        return new Completion(name, completion);
    }

    public static class ActionAndThenBuilder {

        private final String name;
        private final BiFunction<Resource, Request, CompletableFuture<Void>> action;

        public ActionAndThenBuilder(String name, BiFunction<Resource, Request, CompletableFuture<Void>> action) {
            this.name = name;
            this.action = action;
        }

        public Action andThen(Node then) {
            return new Action(name, action, then);
        }
    }

    public static class DecisionOnTrueBuilder {

        private final String name;
        private final BiFunction<Resource, Request, CompletableFuture<Boolean>> decision;

        public DecisionOnTrueBuilder(String name, BiFunction<Resource, Request, CompletableFuture<Boolean>> decision) {
            this.name = name;
            this.decision = decision;
        }

        public DecisionOnFalseBuilder onTrue(Node onTrue) {
            return new DecisionOnFalseBuilder(name, decision, onTrue);
        }
    }

    public static class DecisionOnFalseBuilder {

        private final String name;
        private final BiFunction<Resource, Request, CompletableFuture<Boolean>> decision;
        private final Node onTrue;

        public DecisionOnFalseBuilder(String name, BiFunction<Resource, Request, CompletableFuture<Boolean>> decision, Node onTrue) {
            this.name = name;
            this.decision = decision;
            this.onTrue = onTrue;
        }

        public Decision onFalse(Node onFalse) {
            return new Decision(name, decision, onTrue, onFalse);
        }
    }
}
