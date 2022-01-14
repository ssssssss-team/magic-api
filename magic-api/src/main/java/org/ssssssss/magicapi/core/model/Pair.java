package org.ssssssss.magicapi.core.model;

import java.util.Objects;

public final class Pair<S, T> {

	private final S first;
	private final T second;

	private Pair(S first, T second) {

		this.first = first;
		this.second = second;
	}

	public static <S, T> Pair<S, T> of(S first, T second) {
		return new Pair<>(first, second);
	}

	public S getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Pair)) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}
}
