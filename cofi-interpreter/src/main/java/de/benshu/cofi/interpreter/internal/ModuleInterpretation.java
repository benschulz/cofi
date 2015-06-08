package de.benshu.cofi.interpreter.internal;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.runtime.Class;
import de.benshu.cofi.runtime.MethodDeclaration;
import de.benshu.cofi.runtime.Module;
import de.benshu.cofi.runtime.Singleton;
import de.benshu.cofi.runtime.context.FqnResolver;
import de.benshu.cofi.types.TemplateType;
import de.benshu.cofi.types.TypeList;

import static de.benshu.commons.core.streams.Collectors.single;

public class ModuleInterpretation {
    private final Module module;
    private final ImmutableList<String> relativeApplicationName;
    private final FqnResolver fqnResolver;
    private final Singletons singletons;
    private final StatementEvaluator statementEvaluator;
    private final ExpressionEvaluator expressionEvaluator;

    public final Utilities util;

    public ModuleInterpretation(Module module, ImmutableList<String> relativeApplicationName) {
        this.module = module;
        this.relativeApplicationName = relativeApplicationName;
        this.fqnResolver = new FqnResolver(() -> module);
        this.singletons = new Singletons();
        this.statementEvaluator = new StatementEvaluator(this);
        this.expressionEvaluator = new ExpressionEvaluator(this);

        this.util = new Utilities(this);
    }

    public void perform() {
        final Singleton mainDeclaration = util.lookUpSingleton(module.getFqn().getDescendant(relativeApplicationName));
        final CofiObject main = singletons.lookUpOrCreate(mainDeclaration);

        final Class immutableList = util.lookUpClass("cofi", "lang", "collect", "EmptyImmutableList");
        final Class string = util.lookUpClass("cofi", "lang", "String");

        final FunctionEvaluator start = FunctionEvaluator.forMethod(this, main, mainDeclaration.getBody().getElements().stream()
                .filter(MethodDeclaration.class::isInstance)
                .map(MethodDeclaration.class::cast)
                .filter(m -> m.getName().equals("start"))
                .collect(single()));

        // TODO This be ugly.
        final TemplateType parameterType = immutableList.getType().apply(TypeList.of(string.getType().apply(TypeList.empty())));
        final CofiObject argument = new CofiObject(parameterType);

        start.evaluate(ImmutableList.of(argument));
    }

    public Module getModule() {
        return module;
    }

    public StatementEvaluator getStatementEvaluator() {
        return statementEvaluator;
    }

    public ExpressionEvaluator getExpressionEvaluator() {
        return expressionEvaluator;
    }

    public Singletons getSingletons() {
        return singletons;
    }

    public FqnResolver getFqnResolver() {
        return fqnResolver;
    }
}
