package com.assignment2.jmh;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class HOHLinkedList<T extends Comparable<T>> {
    /*
     * Concurrent Linked List (Unsorted)
     * Uses Hand-Over-Hand Locking to achieve a concurrent Linked List
     */
    private static class Node<T> {
        T key;
        Node<T> next;
        Node<T> prev;
        final ReentrantLock nodeLock = new ReentrantLock();
        Node(T key, Node<T> prev){
            this.key = key;
            this.next = null;
            this.prev = prev;
        }
    }
    private Node<T> head;
    private final AtomicInteger count;

    public HOHLinkedList(){ count = new AtomicInteger(0); }

    // inserts element at given index in list OR at the end
    // of the list if given index is larger than list
    public boolean insertAt(T key, int ind){
        if(ind < 0) return false;
        else if(ind == 0){
            head.nodeLock.lock();
            Node<T> newNode = new Node<>(key, null);
            head.prev = newNode;
            newNode.next = head;
            head = newNode;
            head.next.nodeLock.unlock();
            count.getAndIncrement();
            return true;
        }

        Node<T> curr = head;
        curr.nodeLock.lock();
        int i = 0;
        for(;;){
            if(curr.next == null){
                curr.nodeLock.unlock();
                curr.next = new Node<T>(key, curr);
                count.getAndIncrement();
                return true;
            }else if(i == ind-1){
                curr.nodeLock.unlock();
                curr.next.nodeLock.lock();

                Node<T> newNode = new Node<T>(key, curr);
                newNode.next = curr.next;
                curr.next.prev = newNode;
                curr.next = newNode;

                newNode.next.nodeLock.unlock();
                count.getAndIncrement();
                return true;
            }else{
                i++;
                curr.next.nodeLock.lock();
                curr.nodeLock.unlock();
                curr = curr.next;
            }
        }
    }

    // adds element to beginning of the list
    public void prepend(T key){
        if(head == null){
            head = new Node<>(key, null);
        }
        else{
            head.nodeLock.lock();

            Node<T> newNode = new Node<>(key, null);
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
            head.next.nodeLock.unlock();

        }
        count.getAndIncrement();
    }

    // insert at end of list
    public void append(T key) {
        if (head == null){
            head = new Node<T>(key, null);
            count.getAndIncrement();
        } else {

            Node<T> curr = head;
            curr.nodeLock.lock();
            for(;;){
                if(curr.next == null){
                    curr.nodeLock.unlock();
                    curr.next = new Node<T>(key, curr);
                    count.getAndIncrement();
                    return;
                }else{
                    curr.next.nodeLock.lock();
                    curr.nodeLock.unlock();
                    curr = curr.next;
                }
            }

        }
    }

    public boolean removeAt(int ind){
        if(head == null || count.get() < ind)return false;
        if(ind == 0){
            head.nodeLock.lock();
            Node<T> oldHead = head;
            if(oldHead.next == null){
                head = null;
                count.getAndDecrement();
                return true;
            }
            oldHead.next.nodeLock.lock();
            oldHead.next.prev = null;
            head = oldHead.next;
            head.nodeLock.unlock();
            count.getAndDecrement();
            return true;
        }


        int i = 0;
        Node<T> curr = head;
        curr.nodeLock.lock();
        // stops when curr.next is node to be removed
        while(i < ind-1 && curr.next != null){
            curr.next.nodeLock.lock();
            curr.nodeLock.unlock();
            curr = curr.next;
            i++;
        }
        if(curr.next == null)return false;
        curr.next.nodeLock.lock();
        Node<T> toBeRemoved = curr.next;
        if(toBeRemoved.next != null){
            toBeRemoved.next.nodeLock.lock();
            Node<T> temp = toBeRemoved.next;
            temp.prev = curr;
            curr.next = temp;
            temp.nodeLock.unlock();
        }else{
            curr.next = null;
        }

        count.getAndDecrement();
        toBeRemoved.nodeLock.unlock();
        curr.nodeLock.unlock();

        return true;
    }

    public T get(int ind){
        if(ind < 0) return null;
        else if(ind == 0) return head.key;
        int i = 0;
        for(Node<T> curr = head; curr != null; curr = curr.next){
            if(i == ind) return curr.key;
            i++;
        }
        return null;
    }

    public T getByString(String str){
        for(Node<T> curr = head; curr != null; curr = curr.next){
            if(curr.key.toString().compareTo(str) == 0)return curr.key;
        }
        return null;
    }

    public boolean contains(T key){
        for(Node<T> curr = head; curr != null; curr = curr.next){
            if(curr.key.compareTo(key) == 0)return true;
        }
        return false;
    }

    public int size(){ return count.intValue(); }

    @Override
    public String toString(){
        StringBuilder out = new StringBuilder();
        out.append("[");
        for(Node<T> curr = head; curr != null; curr = curr.next){
            out.append(curr.key.toString());
            if(curr.next != null)out.append(", ");
        }
        out.append("]");
        return out.toString();
    }

}
