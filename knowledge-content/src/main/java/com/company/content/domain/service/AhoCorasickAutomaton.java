package com.company.content.domain.service;

import java.util.*;

public class AhoCorasickAutomaton {

    private final Node root = new Node();

    private static class Node {
        final Map<Character, Node> children = new HashMap<>();
        Node fail;
        String word;
    }

    public void build(List<String> words) {
        // Build Trie
        for (String word : words) {
            Node cur = root;
            for (char c : word.toCharArray()) {
                cur = cur.children.computeIfAbsent(c, k -> new Node());
            }
            cur.word = word;
        }
        // Build fail pointers (BFS)
        Queue<Node> queue = new LinkedList<>();
        root.fail = root;
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.offer(child);
        }
        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            for (Map.Entry<Character, Node> e : cur.children.entrySet()) {
                char c = e.getKey();
                Node child = e.getValue();
                Node f = cur.fail;
                while (f != root && !f.children.containsKey(c)) {
                    f = f.fail;
                }
                if (f.children.containsKey(c) && f.children.get(c) != child) {
                    child.fail = f.children.get(c);
                } else {
                    child.fail = root;
                }
                queue.offer(child);
            }
        }
    }

    public List<MatchResult> match(String text) {
        List<MatchResult> results = new ArrayList<>();
        Node cur = root;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            while (cur != root && !cur.children.containsKey(c)) {
                cur = cur.fail;
            }
            if (cur.children.containsKey(c)) {
                cur = cur.children.get(c);
            }
            // Check all fail chain for matches
            for (Node node = cur; node != root; node = node.fail) {
                if (node.word != null) {
                    int end = i + 1;
                    int start = end - node.word.length();
                    results.add(new MatchResult(node.word, start, end));
                }
            }
        }
        return results;
    }

    public boolean containsAny(String text) {
        Node cur = root;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            while (cur != root && !cur.children.containsKey(c)) {
                cur = cur.fail;
            }
            if (cur.children.containsKey(c)) {
                cur = cur.children.get(c);
            }
            Node check = cur;
            while (check != root) {
                if (check.word != null) return true;
                check = check.fail;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return root.children.isEmpty();
    }

    public record MatchResult(String word, int start, int end) {}
}
