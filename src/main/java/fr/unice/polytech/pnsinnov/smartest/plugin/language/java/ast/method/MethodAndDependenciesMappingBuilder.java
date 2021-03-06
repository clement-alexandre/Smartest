
package fr.unice.polytech.pnsinnov.smartest.plugin.language.java.ast.method;

import fr.smartest.plugin.Module;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class MethodAndDependenciesMappingBuilder implements MappingBuilder {
    private static final Logger logger = LogManager.getLogger(MethodAndDependenciesMappingBuilder.class);

    @Override
    public SourceTestMapping build(Module module, CtModel model) {
        Mapping mapping = new Mapping();
        DependencyMap dependencies = buildDependencyMap(model);
        List<CtMethod> methods = model.getElements(new TypeFilter<>(CtMethod.class));
        for (CtMethod method : methods) {
            if (isTest(module, method)) {
                addTestToMapping(mapping, dependencies, method);
            }
        }
        return new SourceTestMapping(mapping);
    }

    protected DependencyMap buildDependencyMap(CtModel model) {
        DependencyMap dependencies = new DependencyMap();
        List<CtExecutable> executables = model.getRootPackage()
                .filterChildren(new TypeFilter<>(CtExecutable.class)).list();
        for (CtExecutable executable : executables) {
            addDependencies(dependencies, executable);
        }
        return dependencies.inverse();
    }

    protected void addDependencies(DependencyMap dependencies, CtExecutable executable) {
        if (!dependencies.containsKey(executable)) {
            List<CtExecutable> children = getChildren(executable);
            dependencies.put(executable, new HashSet<>(children));
            for (CtExecutable child : children) {
                addDependencies(dependencies, child);
                Set<CtExecutable> ctExecutables = dependencies.get(child);
                dependencies.get(executable).addAll(ctExecutables);
            }
        }
    }

    private List<CtExecutable> getChildren(CtExecutable ctExecutable) {
        if (ctExecutable != null) {
            return ctExecutable.filterChildren(new TypeFilter<>(CtExecutableReference.class))
                    .<CtExecutableReference, CtExecutable>map(CtExecutableReference::getDeclaration).list();
        }
        return new ArrayList<>();
    }

    private void addTestToMapping(Mapping mapping, DependencyMap dependencies, CtMethod test) {
        for (Map.Entry<CtExecutable, Set<CtExecutable>> entry : dependencies.entrySet()) {
            if (entry.getValue().contains(test)) {
                mapping.putIfAbsent(entry.getKey(), new HashSet<>());
                mapping.get(entry.getKey()).add(test);
            }
        }
    }

    private boolean isTest(Module module, CtMethod t) {
        return t != null && t.getPosition().getFile().toPath().startsWith(module.getTestPath());
    }

    private class Mapping extends HashMap<CtExecutable, Set<CtMethod>> {
    }
}
