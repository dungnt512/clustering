package bca.entity.data;

import lombok.Getter;
import lombok.Setter;

public class LinkedNode<E> {
    public final E value;
    public LinkedNode<E> next;
    public LinkedNode<E> previous;

    public LinkedNode(E value, LinkedNode<E> next, LinkedNode<E> previous) {
        this.value = value;
        this.next = next;
        this.previous = previous;
    }
}
