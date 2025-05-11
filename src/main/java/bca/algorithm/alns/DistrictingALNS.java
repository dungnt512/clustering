package bca.algorithm.alns;

import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.FileReader;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

class SegmentGetPos {
    int n;
    int[] tree;
    SegmentGetPos(int n) {
        this.n = n;
        tree = new int[4 * n + 1];
        Arrays.fill(tree, 0);
    }
    void build(int l, int r, int node) {
        if (l == r) {
            tree[node] = 1;
            return;
        }
        int mid = (l + r) / 2;
        build(l, mid, node * 2);
        build(mid + 1, r, node * 2 + 1);
        tree[node] = tree[node * 2] + tree[node * 2 + 1];
    }
    void update(int l, int r, int node, int u) {
        if (r < u || u < l)
            return;
        if (l == r) {
            tree[node] --;
            return;
        }
        int mid = (l + r) / 2;
        if (u <= mid)
            update(l, mid, node * 2, u);
        else
            update(mid + 1, r, node * 2 + 1, u);
        tree[node] = tree[node * 2] + tree[node * 2 + 1];
    }
    int getPos(int l, int r, int node, int u) {
        if (l == r)
            return l;
        int mid = (l + r) / 2;
        if (tree[node * 2] >= u)
            return getPos(l, mid, node * 2, u);
        return getPos(mid + 1, r, node * 2 + 1, u - tree[node * 2]);
    }
}

class SegmentGetMinValue {
    int n;
    double[] tree;
    SegmentGetMinValue(int n, int sizeTree) {
        this.n = n;
        tree = new double[sizeTree + 1];
        Arrays.fill(tree, 1e9);
    }
    void update(int node, double val) {
        tree[node] = val;
        while (node > 1) {
            node /= 2;
            tree[node] = Math.min(tree[node * 2], tree[node * 2 + 1]);
        }
    }
    int getPosMinValue() {
        int l = 1, r = n, node = 1;
        while (l < r) {
            int mid = (l + r) / 2;
            if (tree[node * 2] < tree[node * 2 + 1]) {
                r = mid;
                node = node * 2;
            }
            else {
                l = mid + 1;
                node = node * 2 + 1;
            }
        }
        return l;
    }
}

class Node {
    int id, idCluster, positionInCLuster;
    double x, y, dispersion;
    double[] c = new double[2];
    Node(int id, double x, double y, double customers, double orders, double dispersion) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.c[0] = customers;
        this.c[1] = orders;
        this.dispersion = dispersion;
        this.idCluster = -1;
        this.positionInCLuster = -1;
    }
}

class Edge {
    int u, v;
    Edge(int u, int v) {
        this.u = u;
        this.v = v;
    }
}

class ExpectationValue {
    double min, max;
    ExpectationValue(double min, double max) {
        this.min = min;
        this.max = max;
    }
}

class Cluster {
    int id;
    List <Integer> nodes;
    double[] sumC = new double[2];
    double sumCExpectationValue;
    int sumFamiliarityDeviation;
    double minDispersion;
    Cluster(int id, ExpectationValue[] expectationValues) {
        this.id = id;
        nodes = new ArrayList <> ();
        sumC[0] = 0;
        sumC[1] = 0;
        sumCExpectationValue = 0;
        for (int type = 0; type < 2; type ++)
            sumCExpectationValue += expectationValues[type].min - sumC[type];
        minDispersion = 0;
        sumFamiliarityDeviation = 0;
    }
    //---------------------------------------------------------------------------------------------------
    double getTotalCostOfCluster() {
        return sumCExpectationValue * 1000 + sumFamiliarityDeviation * 500 + minDispersion;
    }
    double getNewTotalCostOfClusterAfterAddNode(Node node, ExpectationValue[] expectationValues, int[][] familiarity, Node[] listNode, double[][] distance) {
        double totalCost = 0;
        totalCost += getNewSumCAfterAddNode(node, expectationValues) * 1000;
        totalCost += getNewSumFamiliarityDeviationAfterAddNode(node, familiarity) * 500;
        totalCost += getNewMinDispersionAfterAddNode(node, listNode, distance);
        return totalCost;
    }
    double getNewTotalCostOfClusterAfterRemoveNode(Node node, ExpectationValue[] expectationValues, int[][] familiarity, Node[] listNode, double[][] distance) {
        double totalCost = 0;
        totalCost += getNewSumCAfterRemoveNode(node, expectationValues) * 1000;
        totalCost += getNewSumFamiliarityDeviationAfterRemoveNode(node, familiarity) * 500;
        totalCost += getNewMinDispersionAfterRemoveNode(node, listNode, distance);
        return totalCost;
    }
    //---------------------------------------------------------------------------------------------------
    void updateClusterAfterAddNode(Node node, ExpectationValue[] expectationValues, int[][] familiarity, Node[] listNode, double[][] distance) {
        updateNewSumCAfterAddNode(node, expectationValues);
        updateNewSumFamiliarityDeviationAfterAddNode(node, familiarity);
        updateNewMinDispersionAfterAddNode(node, listNode, distance);
        node.idCluster = id;
        node.positionInCLuster = nodes.size();
        nodes.add(node.id);
    }
    void updateClusterAfterRemoveNode(Node node, ExpectationValue[] expectationValues, int[][] familiarity, Node[] listNode, double[][] distance) {
        if (nodes.size() == 0)
            return;
        updateNewSumCAfterRemoveNode(node, expectationValues);
        updateNewSumFamiliarityDeviationAfterRemoveNode(node, familiarity);
        updateNewMinDispersionAfterRemoveNode(node, listNode, distance);
        node.idCluster = -1;
        node.positionInCLuster = -1;
        Integer idNodeInteger = node.id;
        nodes.remove(idNodeInteger);
    }
    //---------------------------------------------------------------------------------------------------
    void randomRemovalCluster(int numberNodesRemove, ExpectationValue[] expectationValues, int[][] familiarity, Node[] listNode, double[][] distance, List <List <Integer>> adjacent) {
        List <Integer> listNodesRemove = randomNodesRemove(numberNodesRemove, listNode, adjacent);
        for (int i = 0; i < listNodesRemove.size(); i ++) {
            int u = listNodesRemove.get(i);
            updateClusterAfterRemoveNode(listNode[u], expectationValues, familiarity, listNode, distance);
        }
    }
    List <Integer> randomNodesRemove(int numberNodesRemove, Node[] listNode, List <List <Integer>> adjacent) {
        List <Integer> listNodesCanChoose = new ArrayList <> ();
        int sizeNodes = nodes.size(), numberNodesCanChoose = 1,
            randomValue = rand(0, sizeNodes - 1);
        listNodesCanChoose.add(randomValue);
        SegmentGetPos seg = new SegmentGetPos(sizeNodes);
        seg.build(1, sizeNodes, 1);
        int[] visited = new int[sizeNodes],
              inListNodesCanChoose = new int[sizeNodes];
        Arrays.fill(visited, 0);
        Arrays.fill(inListNodesCanChoose, 0);
        inListNodesCanChoose[randomValue] = 1;
        List <List <Integer>> adj = buildAdj(listNode, adjacent);
        for (int time = 0; time < sizeNodes - numberNodesRemove; time ++)
            numberNodesCanChoose += randomNodeInCLuster(numberNodesCanChoose - time, listNodesCanChoose, seg, visited, inListNodesCanChoose, adj);
        List <Integer> listNodesRemove = new ArrayList <> ();
        for (int i = 0; i < sizeNodes; i ++)
            if (visited[i] == 0)
                listNodesRemove.add(nodes.get(i));
        return listNodesRemove;
    }
    List <List <Integer>> buildAdj(Node[] listNode, List <List <Integer>> adjacent) {
        int sizeNodes = nodes.size();
        List <List <Integer>> adj = new ArrayList <> ();
        for (int i = 0; i < sizeNodes; i ++)
            adj.add(new ArrayList <> ());
        for (int i = 0; i < sizeNodes; i ++) {
            int u = nodes.get(i);
            for (int idAdjacent = 0; idAdjacent < adjacent.get(u).size(); idAdjacent ++) {
                int v = adjacent.get(u).get(idAdjacent);
                if (listNode[v].idCluster == id) {
                    int j = listNode[v].positionInCLuster;
                    adj.get(i).add(j);
                }
            }
        }
        return adj;
    }
    int randomNodeInCLuster(int numberNodesCanChoose, List <Integer> listNodesCanChoose, SegmentGetPos seg, int[] visited, int[] inListNodesCanChoose, List <List <Integer>> adj) {
        int sizeNodes = nodes.size(),
            randomValue = seg.getPos(1, sizeNodes, 1, rand(1, numberNodesCanChoose)) - 1,
            idNode = listNodesCanChoose.get(randomValue),
            numberNodesAdd = 0;
        visited[idNode] = 1;
        for (int idAdj = 0; idAdj < adj.get(idNode).size(); idAdj ++) {
            int i = adj.get(idNode).get(idAdj);
            if (inListNodesCanChoose[i] == 0) {
                numberNodesAdd ++;
                listNodesCanChoose.add(i);
                inListNodesCanChoose[i] = 1;
            }
        }
        seg.update(1, sizeNodes, 1, randomValue + 1);
        return numberNodesAdd;
    }
    //---------------------------------------------------------------------------------------------------
    double getNewSumCAfterAddNode(Node node, ExpectationValue[] expectationValues) {
        double ans = 0;
        for (int type = 0; type < 2; type ++)
            ans += expectationValueOfCType(type, sumC[type] + node.c[type], expectationValues);
        return ans;
    }
    double getNewSumCAfterRemoveNode(Node node, ExpectationValue[] expectationValues) {
        double ans = 0;
        for (int type = 0; type < 2; type ++)
            ans += expectationValueOfCType(type, sumC[type] - node.c[type], expectationValues);
        return ans;
    }
    void updateNewSumCAfterAddNode(Node node, ExpectationValue[] expectationValues) {
        sumCExpectationValue = 0;
        for (int type = 0; type < 2; type ++) {
            sumC[type] += node.c[type];
            sumCExpectationValue += expectationValueOfCType(type, sumC[type], expectationValues);
        }
    }
    void updateNewSumCAfterRemoveNode(Node node, ExpectationValue[] expectationValues) {
        sumCExpectationValue = 0;
        for (int type = 0; type < 2; type ++) {
            sumC[type] -= node.c[type];
            sumCExpectationValue += expectationValueOfCType(type, sumC[type], expectationValues);
        }
    }
    double expectationValueOfCType(int type, double val, ExpectationValue[] expectationValues) {
        double ans = 0;
        if (val < expectationValues[type].min)
            ans += expectationValues[type].min - val;
        else if (val > expectationValues[type].max)
            ans += val - expectationValues[type].max;
        return ans;
    }
    //---------------------------------------------------------------------------------------------------
    double getNewSumFamiliarityDeviationAfterAddNode(Node node, int[][] familiarity) {
        return sumFamiliarityDeviation + familiarity[id][node.id] - 1;
    }
    double getNewSumFamiliarityDeviationAfterRemoveNode(Node node, int[][] familiarity) {
        return sumFamiliarityDeviation - familiarity[id][node.id] + 1;
    }
    void updateNewSumFamiliarityDeviationAfterAddNode(Node node, int[][] familiarity) {
        sumFamiliarityDeviation += familiarity[id][node.id] - 1;
    }
    void updateNewSumFamiliarityDeviationAfterRemoveNode(Node node, int[][] familiarity) {
        sumFamiliarityDeviation -= familiarity[id][node.id] - 1;
    }
    //---------------------------------------------------------------------------------------------------
    double getNewMinDispersionAfterAddNode(Node node, Node[] listNode, double[][] distance) {
        double ans = 1e9, dispersionNode = node.dispersion;
        for (int i = 0; i < nodes.size(); i ++) {
            int idNode = nodes.get(i);
            dispersionNode += distance[node.id][idNode];
            ans = Math.min(ans, listNode[idNode].dispersion + distance[node.id][idNode]);
        }
        return Math.min(ans, dispersionNode);
    }
    double getNewMinDispersionAfterRemoveNode(Node node, Node[] listNode, double[][] distance) {
        double ans = 1e9;
        for (int i = 0; i < nodes.size(); i ++) {
            int idNode = nodes.get(i);
            if (idNode == node.id)
                continue;
            ans = Math.min(ans, listNode[idNode].dispersion - distance[node.id][idNode]);
        }
        return ans;
    }
    void updateNewMinDispersionAfterAddNode(Node node, Node[] listNode, double[][] distance) {
        minDispersion = 1e9;
        for (int i = 0; i < nodes.size(); i ++) {
            int idNode = nodes.get(i);
            listNode[idNode].dispersion += distance[node.id][idNode];
            node.dispersion += distance[node.id][idNode];
            minDispersion = Math.min(minDispersion, listNode[idNode].dispersion);
        }
        minDispersion = Math.min(minDispersion, node.dispersion);
    }
    void updateNewMinDispersionAfterRemoveNode(Node node, Node[] listNode, double[][] distance) {
        minDispersion = 1e9;
        for (int i = 0; i < nodes.size(); i ++) {
            int idNode = nodes.get(i);
            if (idNode == node.id)
                continue;
            listNode[idNode].dispersion -= distance[node.id][idNode];
            node.dispersion -= distance[node.id][idNode];
            minDispersion = Math.min(minDispersion, listNode[idNode].dispersion);
        }
    }
    //---------------------------------------------------------------------------------------------------
    void updatePositionOfNodesInCluster(Node[] listNode) {
        for (int i = 0; i < nodes.size(); i ++) {
            int u = nodes.get(i);
            listNode[u].positionInCLuster = i;
        }
    }
    //---------------------------------------------------------------------------------------------------
    static int rand(int l, int r) {
        Random rd = new Random();
        return l + rd.nextInt(r - l + 1);
    }
}

class Solution {
    Node[] nodes;
    Cluster[] clusters;
    Solution() {
    }
}

class RegretK {
    int k, numberClusters;
    double[] totalCostOfCluster;
    int[] bestClusterTotalCost;
    RegretK(int k, int numberClusters) {
        this.k = k;
        this.numberClusters = numberClusters;
        totalCostOfCluster = new double[numberClusters];
        bestClusterTotalCost = new int[k];
        Arrays.fill(totalCostOfCluster, 1e9);
    }
    void buildBestClusterTotalCost() {
        Arrays.fill(bestClusterTotalCost, -1);
        for (int idCluster = 0; idCluster < numberClusters; idCluster ++)
            for (int i = 0; i < k; i ++)
                if (bestClusterTotalCost[i] < 0 || totalCostOfCluster[idCluster] < totalCostOfCluster[bestClusterTotalCost[i]]) {
                    for (int j = i + 1; j < k; j ++)
                        bestClusterTotalCost[j] = bestClusterTotalCost[j - 1];
                    bestClusterTotalCost[i] = idCluster;
                    break;
                }
    }
    double getRegret() {
        double regret = 0;
        for (int i = 1; i < k; i ++)
            regret += totalCostOfCluster[bestClusterTotalCost[i]] - totalCostOfCluster[bestClusterTotalCost[0]];
        return regret;
    }
}

public class DistrictingALNS {
    static Kattio io;
    static {
        try {
            io = new Kattio("");
        }
        catch (IOException e) {
            e.printStackTrace();          
        }
    }
    static int n, m, numberDrivers, numberClusters;
    static double[] tolarance, average;
    static int[][] familiarity;
    static double[][] distance;
    static boolean[][] connected;
    static Node[] nodes;
    static Edge[] edges;
    static ExpectationValue[] expectationValues;
    static List <List <Integer>> adjacent;
    static int[] posNodeInSegmentTree;

    public static void main(String args[]) {
        readInput();
        initAverageValues();
        buildDistanceMatrix();
        Solution initialSolution = buildInitialSolution();
        checkSolution(initialSolution);
        io.println("----------------------------------");
        Solution solutionNew = copySolution(initialSolution);
        worstRemoval(solutionNew);
        checkSolution(solutionNew);
        List <Integer> listNodesNotInCluster = buildListNodesNotInCluster(solutionNew);
        io.print("List nodes not in cluster: ");
        for (int i = 0; i < listNodesNotInCluster.size(); i ++)
            io.print(listNodesNotInCluster.get(i) + " ");
        io.println();
        io.println("Size list node remove: " + listNodesNotInCluster.size());
        regretKInsertion(solutionNew);
        checkSolution(solutionNew);
        listNodesNotInCluster = buildListNodesNotInCluster(solutionNew);
        io.print("List nodes not in cluster: ");
        for (int i = 0; i < listNodesNotInCluster.size(); i ++)
            io.print(listNodesNotInCluster.get(i) + " ");
        io.println();
        io.println("Size list node remove: " + listNodesNotInCluster.size());
        io.close();
    }
    //---------------------------------------------------------------------------------------------------
    static void readInput() {
        n = io.getInt();
        nodes = new Node[n];
        for (int i = 0; i < n; i ++) {
            int id = io.getInt();
            double x = io.getDouble(),
                   y = io.getDouble(),
                   customers = io.getDouble(),
                   orders = io.getDouble();
            int zero1 = io.getInt(),
                zero2 = io.getInt();
            nodes[i] = new Node(id, x, y, customers, orders, 0);
        }
        m = io.getInt();
        edges = new Edge[m];
        adjacent = new ArrayList <> ();
        for (int i = 0; i < n; i ++)
            adjacent.add(new ArrayList <> ());
        connected = new boolean[n][n];
        for (int i = 0; i < m; i ++) {
            int u = io.getInt(), v = io.getInt();
            edges[i] = new Edge(u, v);
            adjacent.get(u).add(v);
            adjacent.get(v).add(u);
            connected[u][v] = connected[v][u] = true;
        }
        numberDrivers = io.getInt();
        numberClusters = io.getInt();
        tolarance = new double[2];
        for (int i = 0; i < 2; i ++)
            tolarance[i] = io.getDouble();
        double zero1 = io.getDouble(),
               zero2 = io.getDouble();
        familiarity = new int[numberClusters][n];
        for (int i = 0; i < numberClusters; i ++)
            for (int j = 0; j < n; j ++)
                familiarity[i][j] = io.getInt();
        posNodeInSegmentTree = new int[n + 1];
        buildPosNodeInSegmentTree(1, n, 1);
    }
    //---------------------------------------------------------------------------------------------------
    static void buildPosNodeInSegmentTree(int l, int r, int node) {
        if (l == r) {
            posNodeInSegmentTree[l] = node;
            return;
        }
        int mid = (l + r) / 2;
        buildPosNodeInSegmentTree(l, mid, node * 2);
        buildPosNodeInSegmentTree(mid + 1, r, node * 2 + 1);
    }
    static int sizeSegmentTree(int size) {
        int ans = posNodeInSegmentTree[size];
        for (int i = 1; i < size; i ++)
            ans = Math.max(ans, posNodeInSegmentTree[i]);
        return ans;
    }
    //---------------------------------------------------------------------------------------------------
    static Solution buildInitialSolution() {
        Solution solutionCurrent = new Solution();
        solutionCurrent.nodes = copyNode(nodes);
        solutionCurrent.clusters = new Cluster[numberClusters];
        for (int i = 0; i < numberClusters; i ++)
            solutionCurrent.clusters[i] = new Cluster(i, expectationValues);
        greedyInsertion(solutionCurrent);
        return solutionCurrent;
    }
    //---------------------------------------------------------------------------------------------------
    static void buildDistanceMatrix() {
        distance = new double[n][n];
        for (int i = 0; i < n; i ++)
            for (int j = 0; j < n; j ++)
                distance[i][j] = distanceEuclid(nodes[i], nodes[j]);
    }

    static double distanceEuclid(Node a, Node b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }
    //---------------------------------------------------------------------------------------------------
    static void initAverageValues() {
        average = new double[2];
        average[0] = 0;
        average[1] = 0;
        for (int i = 0; i < n; i ++)
            for (int type = 0; type < 2; type ++)
                average[type] += nodes[i].c[type];
        for (int type = 0; type < 2; type ++)
            average[type] /= numberDrivers;
        expectationValues = new ExpectationValue[2];
        for (int type = 0; type < 2; type ++)
            expectationValues[type] = new ExpectationValue(average[type] - average[type] * tolarance[type], average[type] + average[type] * tolarance[type]);
    }
    //---------------------------------------------------------------------------------------------------
    static Solution copySolution(Solution solutionSource) {
        Solution solutionSink = new Solution();
        solutionSink.nodes = copyNode(solutionSource.nodes);
        solutionSink.clusters = copyCluster(solutionSource.clusters);
        return solutionSink;
    }
    static Node[] copyNode(Node[] nodesSource) {
        Node[] nodesSink = new Node[n];
        for (int i = 0; i < n; i ++) {
            nodesSink[i] = new Node(nodesSource[i].id, nodesSource[i].x, nodesSource[i].y, nodesSource[i].c[0], nodesSource[i].c[1], nodesSource[i].dispersion);
            nodesSink[i].idCluster = nodesSource[i].idCluster;
            nodesSink[i].positionInCLuster = nodesSource[i].positionInCLuster;
        }
        return nodesSink;
    }
    static Cluster[] copyCluster(Cluster[] clustersSource) {
        Cluster[] clustersSink = new Cluster[numberClusters];
        for (int i = 0; i < numberClusters; i ++) {
            clustersSink[i] = new Cluster(clustersSource[i].id, expectationValues);
            clustersSink[i].nodes = new ArrayList <> ();
            for (int j = 0; j < clustersSource[i].nodes.size(); j ++)
                clustersSink[i].nodes.add(clustersSource[i].nodes.get(j));
            clustersSink[i].sumC[0] = clustersSource[i].sumC[0];
            clustersSink[i].sumC[1] = clustersSource[i].sumC[1];
            clustersSink[i].sumCExpectationValue = clustersSource[i].sumCExpectationValue;
            clustersSink[i].minDispersion = clustersSource[i].minDispersion;
            clustersSink[i].sumFamiliarityDeviation = clustersSource[i].sumFamiliarityDeviation;
        }
        return clustersSink;
    }
    //---------------------------------------------------------------------------------------------------
    static int rand(int l, int r) {
        Random rd = new Random();
        return l + rd.nextInt(r - l + 1);
    }
    //---------------------------------------------------------------------------------------------------
    static double getNewTotalCostAfterAddNode(Solution solution, double totalCostCurrent, Node node, Cluster cluster) {
        if (checkConnectedBetweenNodeAndCluster(node, cluster) == false)
            return 1e9;
        return totalCostCurrent - cluster.getTotalCostOfCluster() + cluster.getNewTotalCostOfClusterAfterAddNode(node, expectationValues, familiarity, solution.nodes, distance);
    }
    //---------------------------------------------------------------------------------------------------
    static double getTotalCostOfSolution(Solution solution) {
        double totalCost = 0;
        for (int idCluster = 0; idCluster < numberClusters; idCluster ++)
            totalCost += solution.clusters[idCluster].getTotalCostOfCluster();
        return totalCost;
    }
    //---------------------------------------------------------------------------------------------------
    static boolean checkConnectedBetweenNodeAndCluster(Node node, Cluster cluster) {
        if (cluster.nodes.size() == 0)
            return true;
        for (int i = 0; i < cluster.nodes.size(); i ++) {
            int idNode = cluster.nodes.get(i);
            if (connected[node.id][idNode] == true)
                return true;
        }
        return false;
    }
    static void updatePositionOfNodesInClusters(Solution solution) {
        for (int idCluster = 0; idCluster < numberClusters; idCluster ++)
            solution.clusters[idCluster].updatePositionOfNodesInCluster(solution.nodes);
    }
    //---------------------------------------------------------------------------------------------------
    static void randomRemoval(Solution solution) {
        int eta = rand(n / 10, n / 4);
        List <Integer> listRandom = new ArrayList <> ();
        for (int i = 0; i < n; i ++)
            listRandom.add(i);
        Collections.shuffle(listRandom);
        int[] numberNodesRemove = new int[numberClusters];
        for (int i = 0; i < eta; i ++) {
            int u = listRandom.get(i);
            numberNodesRemove[solution.nodes[u].idCluster] ++;
        }
        for (int idCluster = 0; idCluster < numberClusters; idCluster ++)
            if (numberNodesRemove[idCluster] > 0)
                solution.clusters[idCluster].randomRemovalCluster(numberNodesRemove[idCluster], expectationValues, familiarity, solution.nodes, distance, adjacent);
        updatePositionOfNodesInClusters(solution);
    }
    //---------------------------------------------------------------------------------------------------
    static void routeRemoval(Solution solution) {
        int eta = rand(n / 10, n / 4);
        List <Integer> listRandom = new ArrayList <> ();
        for (int i = 0; i < numberClusters; i ++)
            listRandom.add(i);
        Collections.shuffle(listRandom);
        for (int i = 0; i < numberClusters; i ++) {
            int idCluster = listRandom.get(i);
            if (solution.clusters[idCluster].nodes.size() == 0)
                continue;
            int numberNodesRemove = Math.min(eta, solution.clusters[idCluster].nodes.size());
            solution.clusters[idCluster].randomRemovalCluster(numberNodesRemove, expectationValues, familiarity, solution.nodes, distance, adjacent);
            eta -= numberNodesRemove;
            if (eta == 0)
                break;
        }
        updatePositionOfNodesInClusters(solution);
    }
    //---------------------------------------------------------------------------------------------------
    static void worstRemoval(Solution solution) {
        int eta = rand(n / 10, n / 4);
        int[] degree = new int[n],
              parent = new int[n];
        Arrays.fill(degree, -1);
        Arrays.fill(parent, -1);
        buildDegreeAndParent(degree, parent, solution);
        SegmentGetMinValue seg = new SegmentGetMinValue(n, sizeSegmentTree(n));
        updateSeg(seg, degree, solution);
        for (int time = 0; time < eta; time ++) {
            int idNode = seg.getPosMinValue() - 1;
            updateSegAfterRemoveNode(solution.nodes[idNode], seg, degree, parent, solution);
        }
        updatePositionOfNodesInClusters(solution);
    }
    static void updateSeg(SegmentGetMinValue seg, int[] degree, Solution solution) {
        for (int idCluster = 0; idCluster < numberClusters; idCluster ++)
            updateSegCluster(seg, degree, solution.clusters[idCluster], solution);
    }
    static void updateSegCluster(SegmentGetMinValue seg, int[] degree, Cluster cluster, Solution solution) {
        double totalCostOfCluster = cluster.getTotalCostOfCluster();
        for (int i = 0; i < cluster.nodes.size(); i ++) {
            int idNode = cluster.nodes.get(i);
            if (degree[idNode] == 0)
                seg.update(posNodeInSegmentTree[idNode + 1], cluster.getNewTotalCostOfClusterAfterRemoveNode(solution.nodes[idNode], expectationValues, familiarity, solution.nodes, distance) - totalCostOfCluster);
        }
    }
    static void updateSegAfterRemoveNode(Node node, SegmentGetMinValue seg, int[] degree, int[] parent, Solution solution) {
        int idCluster = node.idCluster;
        solution.clusters[idCluster].updateClusterAfterRemoveNode(node, expectationValues, familiarity, solution.nodes, distance);
        seg.update(posNodeInSegmentTree[node.id + 1], 1e9);
        updateDegreeAfterRemoveNode(node.id, degree, parent, solution);
        updateSegCluster(seg, degree, solution.clusters[idCluster], solution);
    }
    static void buildDegreeAndParent(int[] degree, int[] parent, Solution solution) {
        for (int idCluster = 0; idCluster < numberClusters; idCluster ++)
            buildDegreeAndParentOfCluster(degree, parent, solution.clusters[idCluster], solution);
    }
    static void buildDegreeAndParentOfCluster(int[] degree, int[] parent, Cluster cluster, Solution solution) {
        if (cluster.nodes.size() == 0)
            return;
        int root = cluster.nodes.get(rand(0, cluster.nodes.size() - 1));
        bfsBuildDegreeAndParent(root, degree, parent, solution);
    }
    static void bfsBuildDegreeAndParent(int root, int[] degree, int[] parent, Solution solution) {
        Queue <Integer> queue = new LinkedList <> ();
        degree[root] = 0;
        queue.add(root);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int i = 0; i < adjacent.get(u).size(); i ++) {
                int v = adjacent.get(u).get(i);
                if (degree[v] == -1 && solution.nodes[v].idCluster == solution.nodes[u].idCluster) {
                    parent[v] = u;
                    degree[u] ++;
                    degree[v] = 0;
                    queue.add(v);
                }
            }
        }
    }
    static void updateDegreeAfterRemoveNode(int u, int[] degree, int[] parent, Solution solution) {
        degree[u] --;
        if (parent[u] != -1)
            degree[parent[u]] --;
    }
    //---------------------------------------------------------------------------------------------------
    static List <Integer> buildListNodesNotInCluster(Solution solution) {
        List <Integer> listNodesNotInCluster = new ArrayList <> ();
        for (int i = 0; i < n; i ++)
            if (solution.nodes[i].idCluster < 0)
                listNodesNotInCluster.add(i);
        return listNodesNotInCluster;
    }
    //---------------------------------------------------------------------------------------------------
    static void firstPossibleInsertion(Solution solution) {
        List <Integer> listNodesNotInCluster = buildListNodesNotInCluster(solution);
        Collections.shuffle(listNodesNotInCluster);
        List <Integer> listIdCluster = new ArrayList <> ();
        for (int idCluster = 0; idCluster < numberClusters; idCluster ++)
            listIdCluster.add(idCluster);
        for (int i = 0; i < listNodesNotInCluster.size(); i ++) {
            int idNode = listNodesNotInCluster.get(i);
            Collections.shuffle(listIdCluster);
            for (int j = 0; j < numberClusters; j ++) {
                int idCluster = listIdCluster.get(j);
                if (checkConnectedBetweenNodeAndCluster(solution.nodes[idNode], solution.clusters[idCluster]) == true) {
                    solution.clusters[idCluster].updateClusterAfterAddNode(solution.nodes[idNode], expectationValues, familiarity, solution.nodes, distance);
                    break;
                }
            }
        }
    }
    //---------------------------------------------------------------------------------------------------
    static void greedyInsertion(Solution solution) {
        List <Integer> listNodesNotInCluster = buildListNodesNotInCluster(solution);
        Collections.shuffle(listNodesNotInCluster);
        for (int i = 0; i < listNodesNotInCluster.size(); i ++) {
            int idNode = listNodesNotInCluster.get(i), idClusterBest = -1;
            double bestTotalCost = 1e9, totalCostCurrent = getTotalCostOfSolution(solution);
            for (int idCluster = 0; idCluster < numberClusters; idCluster ++) {
                double totalCost = getNewTotalCostAfterAddNode(solution, totalCostCurrent, solution.nodes[idNode], solution.clusters[idCluster]);
                if (totalCost < bestTotalCost) {
                    bestTotalCost = totalCost;
                    idClusterBest = idCluster;
                }
            }
            if (idClusterBest != -1)
                solution.clusters[idClusterBest].updateClusterAfterAddNode(solution.nodes[idNode], expectationValues, familiarity, solution.nodes, distance);
        }
    }
    //---------------------------------------------------------------------------------------------------
    static void secondBestInsertion(Solution solution) {
        List <Integer> listNodesNotInCluster = buildListNodesNotInCluster(solution);
        Collections.shuffle(listNodesNotInCluster);
        for (int i = 0; i < listNodesNotInCluster.size(); i ++) {
            int idNode = listNodesNotInCluster.get(i), idClusterBest = -1, idClusterSecondBest = -1;
            double bestTotalCost = 1e9, secondBestTotalCost = 1e9, totalCostCurrent = getTotalCostOfSolution(solution);
            for (int idCluster = 0; idCluster < numberClusters; idCluster ++) {
                double totalCost = getNewTotalCostAfterAddNode(solution, totalCostCurrent, solution.nodes[idNode], solution.clusters[idCluster]);
                if (totalCost < bestTotalCost) {
                    secondBestTotalCost = bestTotalCost;
                    idClusterSecondBest = idClusterBest;
                    bestTotalCost = totalCost;
                    idClusterBest = idCluster;
                }
                else if (totalCost < secondBestTotalCost) {
                    secondBestTotalCost = totalCost;
                    idClusterSecondBest = idCluster;
                }
            }
            if (idClusterSecondBest != -1)
                solution.clusters[idClusterSecondBest].updateClusterAfterAddNode(solution.nodes[idNode], expectationValues, familiarity, solution.nodes, distance);
        }
    }
    //---------------------------------------------------------------------------------------------------
    static void regretKInsertion(Solution solution) {
        List <Integer> listNodesNotInCluster = buildListNodesNotInCluster(solution);
        int sizeListNodesNotInCluster = listNodesNotInCluster.size();
        RegretK[] regretKs = new RegretK[sizeListNodesNotInCluster];
        double totalCostCurrent = getTotalCostOfSolution(solution);
        for (int i = 0; i < sizeListNodesNotInCluster; i ++) {
            regretKs[i] = new RegretK(3, numberClusters);
            int idNode = listNodesNotInCluster.get(i);
            for (int idCluster = 0; idCluster < numberClusters; idCluster ++)
                regretKs[i].totalCostOfCluster[idCluster] = getNewTotalCostAfterAddNode(solution, totalCostCurrent, solution.nodes[idNode], solution.clusters[idCluster]);
        }
        int[] visited = new int[sizeListNodesNotInCluster];
        Arrays.fill(visited, 0);
        for (int time = 0; time < sizeListNodesNotInCluster; time ++) {
            int i = getIdNodeBest(solution, regretKs, visited),
                idNode = listNodesNotInCluster.get(i),
                idCluster = regretKs[i].bestClusterTotalCost[0];
            totalCostCurrent = getTotalCostOfSolution(solution);
            visited[i] = 1;
            solution.clusters[idCluster].updateClusterAfterAddNode(solution.nodes[idNode], expectationValues, familiarity, solution.nodes, distance);
            double totalCostNew = getTotalCostOfSolution(solution),
                   delta = totalCostNew - totalCostCurrent;
            updateRegretK(solution, listNodesNotInCluster, regretKs, idCluster, totalCostNew, delta);
        }
    }
    static int getIdNodeBest(Solution solution, RegretK[] regretKs, int[] visited) {
        int idNodeBest = -1;
        double bestRegret = -1;
        for (int i = 0; i < regretKs.length; i ++) {
            if (visited[i] == 1)
                continue;
            regretKs[i].buildBestClusterTotalCost();
            double regretOfNode = regretKs[i].getRegret();
            if (regretOfNode > bestRegret) {
                bestRegret = regretOfNode;
                idNodeBest = i;
            }
        }
        return idNodeBest;
    }
    static void updateRegretK(Solution solution, List <Integer> listNodesNotInCluster, RegretK[] regretKs, int idClusterUpdate, double totalCostCurrent, double delta) {
        for (int i = 0; i < regretKs.length; i ++) {
            int idNode = listNodesNotInCluster.get(i);
            for (int idCluster = 0; idCluster < numberClusters; idCluster ++)
                if (idCluster != idClusterUpdate)
                    regretKs[i].totalCostOfCluster[idCluster] += delta;
                else
                    regretKs[i].totalCostOfCluster[idCluster] = getNewTotalCostAfterAddNode(solution, totalCostCurrent, solution.nodes[idNode], solution.clusters[idCluster]);
        }
    }
    //---------------------------------------------------------------------------------------------------
    static void checkSolution(Solution solution) {
        printSolution(solution);
        checkNumberNodes(solution);
        checkConnected(solution);
        printCompareSolution(solution);
    }
    static void printSolution(Solution solution) {
        io.println("Solution");
        for (int idCluster = 0; idCluster < numberClusters; idCluster ++) {
            io.print("Cluster " + idCluster + ": ");
            for (int i = 0; i < solution.clusters[idCluster].nodes.size(); i ++) {
                int idNode = solution.clusters[idCluster].nodes.get(i);
                io.print(idNode + "(" + solution.nodes[idNode].positionInCLuster + ")" + " ");
            }
            io.println();
        }
    }
    static void checkNumberNodes(Solution solution) {
        int[] visited = new int[n];
        Arrays.fill(visited, -1);
        for (int idCluster = 0; idCluster < numberClusters; idCluster ++) {
            for (int i = 0; i < solution.clusters[idCluster].nodes.size(); i ++) {
                int idNode = solution.clusters[idCluster].nodes.get(i);
                if (visited[idNode] >= 0) {
                    io.println("Error: node " + idNode + " is already visited in cluster " + visited[idNode]);
                    return;
                }
                visited[idNode] = idCluster;
            }
        }
        io.println("Check number nodes: OK");
    }
    static void checkConnected(Solution solution) {
        for (int idCluster = 0; idCluster < numberClusters; idCluster ++) {
            for (int i = 0; i < solution.clusters[idCluster].nodes.size(); i ++) {
                int idNode = solution.clusters[idCluster].nodes.get(i), ok = 0;
                for (int j = 0; j < solution.clusters[idCluster].nodes.size(); j ++) {
                    int idNode2 = solution.clusters[idCluster].nodes.get(j);
                    if (idNode != idNode2 && connected[idNode][idNode2] == true) {
                        ok = 1;
                        break;
                    }
                }
                if (ok == 0) {
                    io.println("Error: cluster " + idCluster + " is not connected with node " + idNode);
                    return;
                }
            }
        }
        io.println("Check connected: OK");
    }
    static void printCompareSolution(Solution solution) {
        double totalCost1 = 0, totalCost2 = getTotalCostOfSolution(solution);
        for (int idCluster = 0; idCluster < numberClusters; idCluster ++) {
            double sumCExpectationValue = calSumCExpectationValue(solution, solution.clusters[idCluster]);
            totalCost1 += sumCExpectationValue * 1000;
            int sumFamiliarityDeviation = calSumFamiliarityDeviation(solution.clusters[idCluster]);
            totalCost1 += sumFamiliarityDeviation * 500;
            double minDispersion = calMinDispersion(solution.clusters[idCluster]);
            totalCost1 += minDispersion;
            io.println("Cluster " + idCluster + " :");
            io.println("sumCExpectationValue = " + sumCExpectationValue + " - " + solution.clusters[idCluster].sumCExpectationValue);
            io.println("sumFamiliarityDeviation = " + sumFamiliarityDeviation + " - " + solution.clusters[idCluster].sumFamiliarityDeviation);
            io.println("minDispersion = " + minDispersion + " - " + solution.clusters[idCluster].minDispersion);
            io.println("totalCostOfCluster = " + (sumCExpectationValue * 1000 + sumFamiliarityDeviation * 500 + minDispersion) + " " + solution.clusters[idCluster].getTotalCostOfCluster());
        }
        io.println("totalCost: " + totalCost1 + " - " + totalCost2);
    }
    static double calSumCExpectationValue(Solution solution, Cluster cluster) {
        double ans = 0;
        double[] sumC = new double[2];
        for (int type = 0; type < 2; type ++)
            for (int i = 0; i < cluster.nodes.size(); i ++) {
                int idNode = cluster.nodes.get(i);
                sumC[type] += solution.nodes[idNode].c[type];
            }
        for (int type = 0; type < 2; type ++) {
            double val = sumC[type];
            if (val < expectationValues[type].min)
                ans += expectationValues[type].min - val;
            else if (val > expectationValues[type].max)
                ans += val - expectationValues[type].max;
        }
        return ans;
    }
    static int calSumFamiliarityDeviation(Cluster cluster) {
        int ans = 0;
        for (int i = 0; i < cluster.nodes.size(); i ++) {
            int idNode = cluster.nodes.get(i);
            ans += familiarity[cluster.id][idNode] - 1;
        }
        return ans;
    }
    static double calMinDispersion(Cluster cluster) {
        double ans = 1e9;
        for (int i = 0; i < cluster.nodes.size(); i ++) {
            int idNode = cluster.nodes.get(i);
            double val = 0;
            for (int j = 0; j < cluster.nodes.size(); j ++) {
                if (i == j)
                    continue;
                int idNode2 = cluster.nodes.get(j);
                val += distance[idNode][idNode2];
            }
            ans = Math.min(ans, val);
        }
        return ans;
    }
}

class Kattio extends PrintWriter {
    public Kattio(InputStream i) {
        super(new BufferedOutputStream(System.out));
        r = new BufferedReader(new InputStreamReader(i));
    }
    
    public Kattio(InputStream i, OutputStream o) {
        super(new BufferedOutputStream(o));
        r = new BufferedReader(new InputStreamReader(i));
    }

    public Kattio(String problemName) throws IOException {
        super(problemName + ".out");
        r = new BufferedReader(new FileReader(problemName + ".inp"));
    }

    public boolean hasMoreTokens() {
        return peekToken() != null;
    }

    public int getInt() {
        return Integer.parseInt(nextToken());
    }

    public double getDouble() { 
        return Double.parseDouble(nextToken());
    }

    public long getLong() {
        return Long.parseLong(nextToken());
    }

    public String getWord() {
        return nextToken();
    }

    private BufferedReader r;
    private String line;
    private StringTokenizer st;
    private String token;

    private String peekToken() {
        if (token == null) 
            try {
                while (st == null || !st.hasMoreTokens()) {
                    line = r.readLine();
                    if (line == null) return null;
                    st = new StringTokenizer(line);
                }
                token = st.nextToken();
            } catch (IOException e) { }
        return token;
    }

    private String nextToken() {
        String ans = peekToken();
        token = null;
        return ans;
    }
}
