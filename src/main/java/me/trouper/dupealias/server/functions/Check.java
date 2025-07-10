package me.trouper.dupealias.server.functions;

import me.trouper.dupealias.DupeContext;

public interface Check<T> extends DupeContext {
    boolean passes(T input);
}
