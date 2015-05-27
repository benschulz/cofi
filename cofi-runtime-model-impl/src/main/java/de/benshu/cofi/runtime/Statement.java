package de.benshu.cofi.runtime;

public interface Statement extends ModelNode {
    <R> R accept(StatementVisitor<R> visitor);
}
