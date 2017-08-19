import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author Created by t.wu on 2017/8/17.
 */
public class Hmm {
    public int maxWordLen = 0;

    public List<String> words = Lists.newArrayList();
    public Map<String, Integer> freq = Maps.newHashMap();

    private List<List<Node>> graph;

    /**
     * 转移概率从标记文件获取
     *
     * @param filePath
     */
    public void init(String filePath) {
        File file = new File(filePath);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                words.add("<s>");
                String[] rowWrods = line.split(" ");
                for (String word : rowWrods) {
                    words.add(word);
                }
                words.add("<e>");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        for (int i = 1; i < words.size(); i++) {
            String key = words.get(i) + "|" + words.get(i - 1);
            if (freq.containsKey(key)) {
                freq.put(key, freq.get(key) + 1);
            } else {
                freq.put(key, 1);
            }
        }
        for (String word : words) {
            if (word.length() > maxWordLen) {
                maxWordLen = word.length();
            }
            if (!freq.containsKey(word)) {
                freq.put(word, 1);
            } else {
                freq.put(word, freq.get(word) + 1);
            }
        }
    }

    /**
     * p(A|B) = p(A,B)/p(B)
     *
     * @param curWord
     * @param condition
     * @return
     */
    private double getTransprob(String curWord, String condition) {
        String key = curWord + "|" + condition;
        if (!freq.containsKey(key)) {
            freq.put(key, 0);
        }
        if (!freq.containsKey(condition)) {
            freq.put(condition, 0);
        }
        double jointFreq = freq.get(key) + 1.0;              // + 1.0 是为了平滑
        double conditionFreq = freq.get(condition) + curWord.length(); // + curWord的长度也是为了平滑
        return jointFreq / conditionFreq;
    }

    private void createGraph(String sentence) {
        graph = Lists.newArrayList();
        Node start = new Node("<s>", 1);
        List<Node> startList = Lists.newArrayList();
        startList.add(start);
        graph.add(startList);
        for (int i = 0; i < sentence.length(); i++) {
            graph.add(Lists.<Node>newArrayList());
        }
        Node end = new Node("<e>", 1);
        List<Node> endList = Lists.newArrayList();
        endList.add(end);
        graph.add(endList);

        for (int i = 0; i < sentence.length(); i++) {
            for (int j = 0; j < maxWordLen; j++) {
                if (i + j > sentence.length()) {
                    break;
                }
                String word = sentence.substring(i, i + j);
                if (freq.containsKey(word)) {
                    Node node = new Node(word, j);
                    graph.get(i + j).add(node);
                }
            }
        }
    }

    private void viterbi() {
        for (int i = 0; i <= graph.size() - 2; i++) {
            for (Node curNode : graph.get(i + 1)) {
                int preLevel = i + 1 - curNode.len;
                Node preNode = graph.get(preLevel).get(0);
                double score = getTransprob(curNode.word, preNode.word);
                score = preNode.bestScore - Math.log(score);
                double maxScore = score;
                curNode.bestScore = score;
                curNode.pre = preNode;
                for (int j = 1; j < graph.get(preLevel).size(); j++) {
                    Node tmpNode = graph.get(preLevel).get(j);
                    score = getTransprob(curNode.word, tmpNode.word);
                    score = score - Math.log(score);
                    if (score > maxScore) {
                        maxScore = score;
                        curNode.pre = tmpNode;
                        curNode.bestScore = score;
                    }
                }
            }
        }
        int size = graph.size();
        Node curNode = graph.get(size - 1).get(0);
        for (int i = 1; i < graph.get(size - 1).size(); i++) {
            if (curNode.bestScore < graph.get(size - 1).get(i).bestScore) {
                curNode = graph.get(size - 1).get(i);
            }
        }
        List<String> result = Lists.newArrayList();
        while (curNode != null) {
            result.add(curNode.word);
            curNode = curNode.pre;
        }
        for (int j = result.size() - 1; j > 0; j--) {
            System.out.println(result.get(j));
        }
    }

    public void tokenize(String sentence) {
        createGraph(sentence);
        viterbi();
    }

    public class Node {
        public String word;
        public int len;
        public double bestScore = 0.0;
        public Node pre = null;

        public Node(String word, int len) {
            this.word = word;
            this.len = len;
        }
    }

    public static void main(String[] args) {
        Hmm hmm = new Hmm();
        hmm.init("D:\\github_repo\\ml\\algorithm\\src\\main\\resources\\RenMinData.txt");
        hmm.tokenize("中国人在纽约开办银行");
    }
}
