package bca.entity.data;

import java.util.List;

public class DoubleLinkedList<E> {
    private LinkedNode<E> head;
    private LinkedNode<E> tail;
    private int length = 0;

    public DoubleLinkedList(E value) {
        head = new LinkedNode<>(value, null, null);
        tail = head;
        length = 1;
    }

    public LinkedNode<E> add(E value) {
        LinkedNode<E> newNode = new LinkedNode<>(value, null, tail);
        tail.next = newNode;
        tail = newNode;
        length++;
        return tail;
    }

    public LinkedNode<E> addAll(List<E> values) {
        LinkedNode<E> tail = this.tail;
        for (E value : values) {
            add(value);
        }
        return tail;
    }

    public LinkedNode<E> addFirst(E value) {
        LinkedNode<E> newNode = new LinkedNode<>(value, head, null);
        head.previous = newNode;
        head = newNode;
        length++;
        return head;
    }

    public LinkedNode<E> insert(int index, E value) {
        if (index > length || index < 0) {
            return null;
        } else if (index == 0) {
            return addFirst(value);
        } else if (index == length) {
            return add(value);
        }

        LinkedNode<E> currentNode = traverseToIndex(index);
        LinkedNode<E> leadNode = currentNode.previous;
        LinkedNode<E> newNode = new LinkedNode<>(value, currentNode, leadNode);
        leadNode.next = newNode;
        currentNode.previous = newNode;
        length++;

        return newNode;
    }

    public LinkedNode<E> removeFirst() {
        if (head == null) {
            return null;
        }
        LinkedNode<E> removedNode = head;
        head = head.next;
        if (head != null) {
            head.previous = null;
        } else {
            tail = null;
        }
        length--;
        return removedNode;
    }

    public LinkedNode<E> removeLast() {
        if (tail == null) {
            return null;
        }
        LinkedNode<E> removedNode = tail;
        tail = tail.previous;
        if (tail != null) {
            tail.next = null;
        } else {
            head = null;
        }
        length--;
        return removedNode;
    }

    public void remove(LinkedNode<E> removedNode) {
        if (removedNode == null) {
            return;
        }
        if (removedNode == head) {
            removeFirst();
            return;
        } else if (removedNode == tail) {
            removeLast();
            return;
        }

        LinkedNode<E> previousNode = removedNode.previous;
        LinkedNode<E> nextNode = removedNode.next;

        if (previousNode != null) {
            previousNode.next = nextNode;
        }
        if (nextNode != null) {
            nextNode.previous = previousNode;
        }
        length--;
    }

    public LinkedNode<E> traverseToIndex(int index) {
        LinkedNode<E> temp = head;
        while (index-- > 0) {
            temp = temp.next;
        }
        return temp;
    }

    public boolean isEmpty() {
        return length == 0;
    }

    public LinkedNode<E> getFirst() {
        return head;
    }
    public LinkedNode<E> getLast() {
        return tail;
    }
}
