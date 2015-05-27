package de.benshu.cofi.runtime;

import de.benshu.commons.core.exception.UnexpectedBranchException;

public interface NamedEntityVisitor<R> {
    default R defaultAction(NamedEntity namedEntity) {
        throw new UnexpectedBranchException();
    }

    default R visitClass(Class klazz) {
        return defaultAction(klazz);
    }

    default R visitCompanion(Companion companion) {
        return defaultAction(companion);
    }

    default R visitModule(Module module) {
        return defaultAction(module);
    }

    default R visitPackage(Package pkg) {
        return defaultAction(pkg);
    }

    default R visitSingleton(ObjectSingleton objectSingleton) {
        return defaultAction(objectSingleton);
    }

    default R visitTrait(Trait trait) {
        return defaultAction(trait);
    }

    default R visitUnion(Union union) {
        return defaultAction(union);
    }
}
