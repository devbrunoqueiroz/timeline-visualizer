package com.chronicle.application.shared;

public interface UseCase<I, O> {
    O execute(I input);
}
