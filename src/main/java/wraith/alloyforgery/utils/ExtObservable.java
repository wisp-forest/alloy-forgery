package wraith.alloyforgery.utils;

import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import io.wispforest.owo.util.Observable;

import java.util.Collection;
import java.util.function.Consumer;

public class ExtObservable<T> extends Observable<T> {

    private final EventStream<Consumer<T>> notifyObservers = new EventStream<>(observers -> (t) -> observers.forEach(observer -> observer.accept(t)));

    protected ExtObservable(T initial) {
        super(initial);
    }

    public static <T> ExtObservable<T> of(T initial) {
        return new ExtObservable<>(initial);
    }

    @Deprecated
    public void observe(Consumer<T> observer) {
        observeSub(observer);
    }

    @Override
    protected void notifyObservers(T value) {
        notifyObservers.sink().accept(value);
    }

    public EventSource.Subscription observeSub(Consumer<T> observer) {
        return notifyObservers.source().subscribe(observer);
    }

    public void markDirty() {
        notifyObservers(this.get());
    }
}
