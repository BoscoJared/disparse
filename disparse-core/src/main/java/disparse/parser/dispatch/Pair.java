package disparse.parser.dispatch;

import java.util.Objects;

public class Pair<T> {
    final private T key;
    final private T val;

    public Pair(final T key, final T val) {
        this.key = key;
        this.val = val;
    }

    public T getKey() {
        return this.key;
    }

    public T getVal() {
        return this.val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?> pair = (Pair<?>) o;
        return key.equals(pair.key) &&
                Objects.equals(val, pair.val);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, val);
    }

    public static <T> Pair<T> of(final T key, final T val){
        return new Pair<>(key, val);
    }
}
