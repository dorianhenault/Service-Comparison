package tmp.uqam.stage.mojo;

import java.util.ArrayList;
import java.util.List;


class BipartiteGraph {

    /*
     * we use a List to represent a edge list in a directed graph for example,
     * adjacentList[1] has 2 means there is a edge from point 1 to point 2
     */

    List<List<Integer>> adjacentList;

    /* vertex list */
    Vertex vertex[];

    /* this list is used to store the augmenting Path we got from matching */
    private List<Integer> augmentPath;

    /* total number of all points,points in left side and right side */
    private int points;
    private int leftpoints;

    /* create the graph,points means the toal number of points */
    BipartiteGraph(int points, int leftpoints, int rightpoints) {
        this.leftpoints = leftpoints;
        this.points = points;

        adjacentList = new ArrayList<>(points);
        vertex = new Vertex[points];
        augmentPath = new ArrayList<>();

        for (int i = 0; i < points; i++) {
            vertex[i] = new Vertex();
            if (i < leftpoints) vertex[i].isLeft = true;
            adjacentList.add(i, new ArrayList<>());
        }

    }

    /* add edge, add an edge to the graph */
    public void addedge(int startPoint, int endPoint) {
        /* insert the edge to the adjacentList of startPoint */
        adjacentList.get(startPoint).add(endPoint);
        /* increase the outdegree of startPoint, indegree of endPoint */
        vertex[startPoint].outdegree += 1;
        vertex[endPoint].indegree += 1;
        /*
         * if the edge is from right to left side, mark both start and end as
         * mached point
         */
        if (isRight(startPoint) && isLeft(endPoint)) {
            vertex[startPoint].matched = true;
            vertex[endPoint].matched = true;

        }
    }

    public void removeEdge(int startPoint, int endPoint) {
        /* find the index of edge in the adjacentList of startPoint */
        int index = adjacentList.get(startPoint).indexOf(endPoint);
        /* remove the edge from adjacentList of startPoint */
        if (index > -1) adjacentList.get(startPoint).remove(index);
        /* decrease the outdegree of startPoint and indegree of endPoint */
        vertex[startPoint].outdegree -= 1;
        vertex[endPoint].indegree -= 1;

        /*
         * if the startPoint is on the right, and its outdegree become zero,
         * mark the startPoint as unmached
         */
        if (isRight(startPoint) && vertex[startPoint].outdegree == 0) vertex[startPoint].matched = false;

        /*
         * if the endPoint is on the left, and its indegree become zero, mark
         * the endPoint as unmached
         */
        if (isLeft(endPoint) && vertex[endPoint].indegree == 0) vertex[endPoint].matched = false;

    }

    /* Change the direction of an edge, e.g. change i -> j to j -> i */
    public void reverseEdge(int startPoint, int endPoint) {
        removeEdge(startPoint, endPoint);
        addedge(endPoint, startPoint);
    }

    /* Reverse all the edges in the augmenting path */
    public String XOR() {
        int start, end;
        String str = "";
        /* the first point of augmenting path */
        start = augmentPath.get(0);

        for (int i = 1; i < augmentPath.size(); i++) {
            end = augmentPath.get(i);
            reverseEdge(start, end);
            start = end;
        }
        return str;
    }

    /* do the maximum bipartiture matching */
    public void matching() {

        StringBuilder str = new StringBuilder();
        while (findAugmentPath()) {
            str.append(XOR());
        }
    }

    private boolean findAugmentPath() {
        augmentPath.clear(); /* init the path */
        /*
         * use all the unmatched left points as start, see if we can find a
         * augmenting path
         */
        for (int i = 0; i < leftpoints; i++) {
            if (!vertex[i].matched) {
                if (findPath(i)) return true;
                else augmentPath.clear(); /* re init the path */
            }
        }
        return false;
    }

    /* recursive find a path using DFS */
    private boolean findPath(int start) {
        int nextPt, index;
        /* if the current vertex has no out edge, return false */
        if (vertex[start].outdegree == 0) return false;
        /* insert the current point to the path */
        augmentPath.add(start);

        /*
         * use the pts that the current point is linked to as next point,
         * recursively call findPath function
         */
        for (int i = 0; i < adjacentList.get(start).size(); i++) {
            nextPt = adjacentList.get(start).get(i);
            /* if the next point was already in the path, discard it */
            if (augmentPath.indexOf(nextPt) > -1) continue;
            /* find a terminal, add it to the path and return true */
            if (!vertex[nextPt].matched) {
                augmentPath.add(nextPt);
                return true;
            }
            /* otherwise recursive call using depth first search */
            else if (findPath(nextPt)) return true;

        }
        /* if failed, delete the current pt from path and return false */
        index = augmentPath.indexOf(start);
        augmentPath.remove(index);
        return false;

    }

    /* indicate whether the current point is in right side */
    private boolean isLeft(int pt) {
        return pt < leftpoints;
    }

    /* indicate whether the current point is in right side */
    private boolean isRight(int pt) {
        return pt > leftpoints - 1;
    }

    /* print out the current status of the graph */
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < points; i++) {
            str.append("Point ");
            str.append(isLeft(i) ? "A" + (i + 1) : "G" + (i - leftpoints + 1));
            str.append(" is ");
            str.append(vertex[i].matched ? "MATCHED\n" : "UNMATCHED\n");
            for (int j = 0; j < adjacentList.get(i).size(); j++) {
                int to = adjacentList.get(i).get(j);
                str.append(" and is connected to points ");
                str.append(isLeft(to) ? "A" + (to + 1) : "G" + (to - leftpoints + 1));
                str.append("\n");
            }
        }
        return str.toString();
    }

}
