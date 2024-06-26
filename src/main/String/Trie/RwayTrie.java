package main.String.Trie;

import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;
import kotlin.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RwayTrie<T> implements Trie<T> {
    private static final int R = 26;
    private static final char WILDCARD = '.';
    private static class Node<T> {
        T value;
        Node[] nexts = new Node[R];
    }

    private Node<T> root;

    @Override
    public void insert(String key, T value) {
        if (value == null) throw new IllegalArgumentException("value can't be null!");
        root = insert(root, key, 0, value);
    }

    private Node<T> insert(Node<T> node, String key, int from, T value) {
        if (node == null) {
            node = new Node<>();
        }
        if (from == key.length()) {
            node.value = value;
        } else {
            node.nexts[key.charAt(from) - 'a'] = insert(node.nexts[key.charAt(from) - 'a'], key, from + 1, value);
        }
        return node;
    }

    @Override
    public T search(String key) {
        Node<T> node = search(root, key, 0);
        if (node == null) {
            return null;
        } else {
            return node.value;
        }
    }

    // find the node corresponds to key
    // the string from root to node is equal to key.substring(0, to)
    // to <= key.length
    private Node<T> search(Node<T> node, String key, int to) {
        if (node == null) return null;
        if (to == key.length()) {
            return node;
        } else {
            return search(node.nexts[key.charAt(to) - 'a'], key, to + 1);
        }
    }

    @Override
    public boolean delete(String key) {
        Pair<Node<T>, Boolean> result = delete(root, key, 0);
        root = result.getFirst();
        return result.getSecond();
    }
    // to <= key.length
    // string from root to node is supposed to be equal to key.substring(0, to)
    private Pair<Node<T>, Boolean> delete(Node node, String key, int to) {
        if (node == null) return new Pair(node, false);
        boolean deleted = false;
        if (to == key.length()) {
            if (node.value != null) {
                node.value = null;
                deleted = true;
            } else {
                return new Pair(node, false);
            }
        } else {
            int targetInd = key.charAt(to) - 'a';
            Pair<Node<T>, Boolean> result = delete(node.nexts[targetInd], key, to + 1);
            node.nexts[targetInd] = result.getFirst();
            deleted = result.getSecond();
        }
        if (node.value != null) return new Pair(node, deleted);
        for (int i = 0; i < R; i ++) {
            if (node.nexts[i] != null) {
                return new Pair(node, deleted);
            }
        }
        return new Pair(null, deleted);
    }

    @Override
    public Iterable<String> keys() {
        List<String> keys = new ArrayList<>();
        if (root == null) return keys;
        collectAll(root, "", keys);
        return keys;
    }

    // the string from root to node is supposed to be equal with curPrefix
    // node != null
    private void collectAll(Node<T> node, String curPrefix, List<String> container) {
        if (node.value != null) {
            container.add(curPrefix);
        }
        for (int i = 0; i < R; i ++) {
            if (node.nexts[i] != null) {
                String newPrefix = String.format("%s%s", curPrefix, (char) (i + 'a'));
                collectAll(node.nexts[i], newPrefix, container);
            }
        }
    }


    /**
     * keys that match {@code s} which has wildcard
     * @param s
     * @return
     */
    @Override
    public Iterable<String> keysThatMatch(String s) {
        List<String> result = new ArrayList<>();
        if (root == null) return result;
        collectMatch(root, "", s, result);
        return result;
    }

    // the string from root to node is supposed to be equal to curprefix
    // prefix.length <= str.length
    // prefix matches str.substring(0, prefix.length)
    // node != null
    private void collectMatch(Node<T> node, String prefix, String str, List<String> container) {
        if (prefix.length() == str.length()) {
            if (node.value != null) {
                container.add(prefix);
            }
        } else {
            char targetChr = str.charAt(prefix.length());
            if (targetChr == WILDCARD) {
                for (int i = 0; i < R; i ++) {
                    if (node.nexts[i] != null) {
                        String newPrefix = String.format("%s%s", prefix, (char) (i + 'a'));
                        collectMatch(node.nexts[i], newPrefix, str, container);
                    }
                }
            } else {
                Node<T> targetNode = node.nexts[targetChr - 'a'];
                if (targetNode != null) {
                    collectMatch(targetNode, String.format("%s%s", prefix, targetChr), str, container);
                }
            }
        }
    }

    @Override
    public Iterable<String> keysWithPrefix(String prefix) {
        Node<T> node = search(root, prefix, 0);
        List<String> keys = new ArrayList<>();
        if (node != null) {
            collectAll(node, prefix, keys);
        }
        return keys;
    }

    @Override
    public String longestPrefixOf(String query) {
        if (root == null) return null;
        int longest = longestMatch(root, query, 0, -1);
        if (longest == -1) {
            return null;
        } else {
            return query.substring(0, longest);
        }
    }

    // the string from root to node is equal to query.substring(0, to)
    // to <= query.length
    // node != null
    private int longestMatch(Node<T> node, String query, int to, int longest) {
        if (node.value != null) longest = to;
        if (to < query.length()) {
            Node targetNode = node.nexts[query.charAt(to) - 'a'];
            if (targetNode != null) {
                return longestMatch(targetNode, query, to + 1, longest);
            }
        }
        return longest;
    }

    public static void main(String[] args) {
        RwayTrie<Integer> trie = new RwayTrie<>();
        int i = 0;
        ArrayList<String> str = new ArrayList<>();
        while (StdIn.hasNextLine()) {
            String word = StdIn.readLine();
            if (word.isEmpty()) break;
            str.add(word);
            trie.insert(word, i ++);
        }
        StdOut.println("test keys --------------------------------");
        Collections.sort(str);
        i = 0;
        for (String key:trie.keys()) {
            if (!key.equals(str.get(i ++))) {
                StdOut.println("keys not match");

            }
        }
        if (i != str.size()) {
            StdOut.println("missing keys in trie");
        }
        StdOut.println("test search -----------------------------");
        while (StdIn.hasNextLine()) {
            String word = StdIn.readLine();
            if (word.isEmpty()) break;
            Integer index = trie.search(word);
            if (index == null) {
                StdOut.println("not found");
            } else {
                StdOut.println("index: " + index);
            }
        }
        StdOut.println("test keysWithPrefix ---------------------");
        String prefix = StdIn.readLine();
        for (String key:trie.keysWithPrefix(prefix)) {
            StdOut.println(key);
        }
        StdOut.println("test keysThatMatch -----------------------");
        String regx = StdIn.readLine();
        for (String key:trie.keysThatMatch(regx)) {
            StdOut.println(key);
        }
        StdOut.println("test longestPrefixOf");
        StdOut.println(trie.longestPrefixOf(StdIn.readLine()));
        StdOut.println("test delete -------------------------");
        while (StdIn.hasNextLine()) {
            String s = StdIn.readLine();
            if (s.isEmpty()) break;
            StdOut.println("delete " + s + " result: " + trie.delete(s));
        }
        StdOut.println("current keys in trie:");
        for (String key:trie.keys()) {
            StdOut.println(key);
        }
    }
}
