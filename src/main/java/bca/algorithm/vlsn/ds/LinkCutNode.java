package bca.algorithm.vlsn.ds;

public class LinkCutNode {
    private LinkCutNode parent;
    private final LinkCutNode[] children = new LinkCutNode[2];
    private LinkCutNode extra;
    private boolean flip;

    private int value;
    private int size = 0;
    private int sub = 0;
    private int virtual_sub = 0;

    public LinkCutNode(int value) {
        this.value = value;
        calculate();
    }

    // friend
    static public int getSize(LinkCutNode node) {
        return node == null ? 0 : node.size;
    }
    static public int getSub(LinkCutNode node) {
        return node == null ? 0 : node.sub;
    }

    private void propagate() {
        if (!flip) {
            return ;
        }
        LinkCutNode temp = children[0];
        children[0] = children[1];
        children[1] = temp;
        flip = false;
        for (LinkCutNode child : children) {
            if (child != null) {
                child.flip = !child.flip;
            }
        }
    }

    private void calculate() {
        for (LinkCutNode child : children) {
            if (child != null) {
                child.propagate();
            }
        }
        size = 1 + getSize(children[0]) + getSize(children[1]);
        sub = value + getSub(children[0]) + getSub(children[1]) + virtual_sub;
    }

    private int direction() {
        if (parent == null) {
            return -2;
        }
        for (int i = 0; i < 2; i++) {
            if (parent.children[i] == this) {
                return i;
            }
        }
        return -1;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isRoot() {
        return direction() < 0;
    }

    // friend
    static public void setLink(LinkCutNode x, LinkCutNode y, int dir) {
        if (y != null) {
            y.parent = x;
        }
        if (dir >= 0) {
            x.children[dir] = y;
        }
    }

    private void rotate() {
        assert !isRoot();
        int dir = direction();
        LinkCutNode par = parent;
        setLink(par.parent, this, par.direction());

        setLink(par, children[dir ^ 1], dir);
        setLink(this, par, dir ^ 1);

        par.calculate();
    }

    private void splay() {
        while (!isRoot() && !parent.isRoot()) {
            parent.parent.rotate();
            parent.rotate();
            rotate();

            if (direction() == parent.direction()) {
                parent.rotate();
            } else {
                rotate();
            }
            rotate();
        }

        if (!isRoot()) {
            parent.propagate();
            propagate();
            rotate();
        }
        propagate();
        calculate();
    }

    private LinkCutNode findByOrder(int order) {
        propagate();
        int size = getSize(children[0]);
        if (order == size) {
            splay();
            return this;
        }
        return order < size ? children[0].findByOrder(order) : children[1].findByOrder(order - size - 1);
    }

    private void access() {
        for (LinkCutNode node = this, previous = null; node != null; node = node.parent) {
            node.splay();
            if (previous != null) {
                node.virtual_sub -= getSub(previous);
            }
            if (node.children[1] != null) {
                node.virtual_sub += getSub(node.children[1]);
            }
            node.children[1] = previous;
            node.calculate();
            previous = node;
        }

        splay();
        assert children[1] == null;
    }

    private void makeRoot() {
        access();
        flip = !flip;
        access();
        assert children[0] == null && children[1] == null;
    }

    public static LinkCutNode lca(LinkCutNode x, LinkCutNode y) {
        if (x == y) {
            return x;
        }
        x.access();
        y.access();
        if (x.parent == null) {
            return null;
        }
        x.splay();
        return x.parent == null ? x : null;
    }
    public static boolean isConnected(LinkCutNode x, LinkCutNode y) {
        return lca(x, y) != null;
    }

    public int distRoot() {
        access();
        return getSize(children[0]);
    }
    public LinkCutNode getRoot() {
        access();
        LinkCutNode node = this;
        while (node.children[0] != null) {
            node = node.children[0];
            node.propagate();
        }
        node.access();
        return node;
    }

    public LinkCutNode getParent(int order) {
        access();
        order = getSize(children[0]) - order;
        assert order >= 0;
        return findByOrder(order);
    }

    public void set(int value) {
        access();
        this.value = value;
        calculate();
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static void link(LinkCutNode x, LinkCutNode y, boolean forced) {
        assert !isConnected(x, y);
        if (forced) {
            y.makeRoot();
        }
        else {
            y.access();
            assert y.children[0] == null;
        }
        x.access();
        setLink(y, x, 0);
        y.calculate();
    }

    public static void cut(LinkCutNode y) {
        y.access();
        assert y.children[0] != null;
        y.children[0].parent = null;
        y.children[0] = null;
        y.calculate();
    }
    public static void cut(LinkCutNode x, LinkCutNode y) {
        x.makeRoot();
        y.access();
        assert(y.children[0] == x && x.children[0] == null && x.children[1] == null);
        cut(y);
    }
}
