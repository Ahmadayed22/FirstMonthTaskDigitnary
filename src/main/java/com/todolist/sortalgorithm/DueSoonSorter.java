package com.todolist.sortalgorithm;

import java.util.ArrayList;
import java.util.List;

import com.todolist.entity.Task;


public class DueSoonSorter {

    public List<Task> sortByDueDateThenPriority(List<Task> tasks) {
        List<Task> copy = new ArrayList<>(tasks);
        if (copy.size() <= 1) {
            return copy;
        }
        Task[] working = copy.toArray(new Task[0]);
        Task[] buffer = new Task[working.length];
        mergeSort(working, buffer, 0, working.length - 1);

        List<Task> result = new ArrayList<>(working.length);
        for (Task t : working) {
            result.add(t);
        }
        return result;
    }

    private void mergeSort(Task[] arr, Task[] buffer, int lo, int hi) {
        if (lo >= hi) {
            return;
        }
        int mid = lo + (hi - lo) / 2;
        mergeSort(arr, buffer, lo, mid);
        mergeSort(arr, buffer, mid + 1, hi);
        merge(arr, buffer, lo, mid, hi);
    }

    private void merge(Task[] arr, Task[] buffer, int lo, int mid, int hi) {
        for (int i = lo; i <= hi; i++) {
            buffer[i] = arr[i];
        }
        int left = lo;
        int right = mid + 1;
        int out = lo;
        while (left <= mid && right <= hi) {
            if (compareTasks(buffer[left], buffer[right]) <= 0) {
                arr[out++] = buffer[left++];
            } else {
                arr[out++] = buffer[right++];
            }
        }
        while (left <= mid) {
            arr[out++] = buffer[left++];
        }
        while (right <= hi) {
            arr[out++] = buffer[right++];
        }
    }

    private int compareTasks(Task a, Task b) {
        int byDate = a.getDueDate().compareTo(b.getDueDate());
        if (byDate != 0) {
            return byDate;
        }
        return Integer.compare(a.getPriority().urgencyRank(), b.getPriority().urgencyRank());
    }
}
