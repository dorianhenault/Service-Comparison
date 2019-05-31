package tmp.uqam.stage.mojo;

import java.util.ArrayList;
import java.util.List;

class Cluster {

    /* No. A */
    private int no = 0;

    /* no. of cluster in A */
    // private int l = 0;
    /* no. of cluster in B */
    private int m = 0;

    /* max V(ij), the maximium tags */
    private int maxtag = 0;

    /* |Ai|, total objects in Ai */
    private int totaltags = 0;

    /* the group No that Ai belongs to */
    private int group = 0;

    /* tags */
    private int tags[];

    /* object list */
    public List<List
            <String>> objectList;

    /* group list */
    public List<Integer> groupList;

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getMaxtag() {
        return maxtag;
    }

    public int gettotalTags() {
        return totaltags;
    }

    public Cluster(int no, int l, int m) {
        // this.l = l;
        this.m = m;
        this.no = no;
        tags = new int[m];
        objectList = new ArrayList<>(m);
        groupList = new ArrayList<>();
        for (int j = 0; j < m; j++) {
            tags[j] = 0;
            objectList.add(j, new ArrayList<>());
        }
    }


    public void addobject(int t, String object) {
        if (t >= 0 && t < m) {
            tags[t] += 1;
            totaltags += 1;
            objectList.get(t).add(object);

            /* if tags is max & unique,then change group to it & clear grouplist */
            if (tags[t] > maxtag) {
                maxtag = tags[t];
                group = t;
                groupList.clear();
                groupList.add(t);
            }
            /* if tags is max but not nuique,then add it to the grouplist */
            else if (tags[t] == maxtag) {
                groupList.add(t);
            }
        }
    }


    public String toString() {
        String str = "";
        str = str + "A" + (no + 1) + " is in group G" + (group + 1) + "\n";

        for (int i = 0; i < m; i++) {
            if (objectList.get(i).size() > 0) {
                str = str + "Group " + (i + 1) + ":" + " have " + objectList.get(i).size() + " objects, they are " + objectList.get(i).toString() + "\n";

            }

        }
        return str;
    }

}
