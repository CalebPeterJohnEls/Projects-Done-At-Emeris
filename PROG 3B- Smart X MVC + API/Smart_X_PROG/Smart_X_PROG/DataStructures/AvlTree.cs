namespace Smart_X_API.DataStructures
{
    // A self-balancing binary search tree. After every insert it checks the
    // height difference between the left and right sides and rotates the
    // nodes if the difference is more than one (Morris, 1998). Readings arrive
    // in time order, which would turn a plain binary search tree into a long
    // chain and make it slow (Baeldung, 2026). The AVL tree stays shallow, so
    // inserts stay fast (Cornell University, 2021).
    public class AvlTree<TKey, TValue> where TKey : IComparable<TKey>
    {
        private AvlNode<TKey, TValue>? root;

        // How many readings are in the tree.
        public int Count { get; private set; }

        // How many levels tall the tree is.
        public int Height => GetHeight(root);

        // Adds a reading. The public method starts at the root.
        public void Insert(TKey key, TValue value)
        {
            root = Insert(root, key, value);
        }

        // Goes down the tree to find the right spot, adds the node as a leaf,
        // then balances every node on the way back up (dev.to
        // (christinamcmahon), 2020).
        private AvlNode<TKey, TValue> Insert(AvlNode<TKey, TValue>? node, TKey key, TValue value)
        {
            if (node == null)
            {
                Count++;
                return new AvlNode<TKey, TValue>(key, value);
            }

            int comparison = key.CompareTo(node.Key);

            if (comparison < 0)
            {
                node.Left = Insert(node.Left, key, value);
            }
            else if (comparison > 0)
            {
                node.Right = Insert(node.Right, key, value);
            }
            else
            {
                // The same key again just replaces the old value.
                node.Value = value;
                return node;
            }

            return Balance(node);
        }

        // Returns every value whose key is between from and to (inclusive),
        // in key order. A whole side of the tree is skipped when it cannot
        // contain a match, so the tree is never searched in full.
        public List<TValue> RangeQuery(TKey from, TKey to)
        {
            return RangeQuery(from, to, out _);
        }

        // The same range query, but it also reports how many nodes it
        // visited, so the page can show how little of the tree was searched.
        public List<TValue> RangeQuery(TKey from, TKey to, out int nodesVisited)
        {
            List<TValue> results = new List<TValue>();
            nodesVisited = 0;
            QueryRange(root, from, to, results, ref nodesVisited);
            return results;
        }

        // Walks only the parts of the tree that could hold a match.
        private void QueryRange(AvlNode<TKey, TValue>? node, TKey from, TKey to, List<TValue> results, ref int visited)
        {
            if (node == null)
            {
                return;
            }

            visited++;

            // Only look left if "from" is smaller than this key.
            if (from.CompareTo(node.Key) < 0)
            {
                QueryRange(node.Left, from, to, results, ref visited);
            }

            // This node is a match if its key is between from and to.
            if (from.CompareTo(node.Key) <= 0 && to.CompareTo(node.Key) >= 0)
            {
                results.Add(node.Value);
            }

            // Only look right if "to" is bigger than this key.
            if (to.CompareTo(node.Key) > 0)
            {
                QueryRange(node.Right, from, to, results, ref visited);
            }
        }

        // Returns all values in key order (an in-order walk of the tree).
        public List<TValue> GetAll()
        {
            List<TValue> results = new List<TValue>();
            InOrder(root, results);
            return results;
        }

        // Left side first, then this node, then the right side.
        private void InOrder(AvlNode<TKey, TValue>? node, List<TValue> results)
        {
            if (node == null)
            {
                return;
            }

            InOrder(node.Left, results);
            results.Add(node.Value);
            InOrder(node.Right, results);
        }

        // An empty spot has height 0.
        private int GetHeight(AvlNode<TKey, TValue>? node)
        {
            return node == null ? 0 : node.Height;
        }

        // A node is one taller than its taller child.
        private void UpdateHeight(AvlNode<TKey, TValue> node)
        {
            node.Height = 1 + Math.Max(GetHeight(node.Left), GetHeight(node.Right));
        }

        // The balance is the left height minus the right height. A balance
        // above 1 means the left side is too tall, below -1 means the right
        // side is too tall (dev.to (christinamcmahon), 2020). A single
        // rotation fixes a straight line of nodes, and a double rotation fixes
        // a zig-zag. There are four cases in total (Rajinikanth, 2026).
        private AvlNode<TKey, TValue> Balance(AvlNode<TKey, TValue> node)
        {
            UpdateHeight(node);
            int balance = GetHeight(node.Left) - GetHeight(node.Right);

            if (balance > 1)
            {
                // Left-right case: turn it into a straight line first.
                if (GetHeight(node.Left!.Left) < GetHeight(node.Left.Right))
                {
                    node.Left = RotateLeft(node.Left);
                }
                return RotateRight(node);
            }

            if (balance < -1)
            {
                // Right-left case: turn it into a straight line first.
                if (GetHeight(node.Right!.Right) < GetHeight(node.Right.Left))
                {
                    node.Right = RotateRight(node.Right);
                }
                return RotateLeft(node);
            }

            return node;
        }

        // The left child moves up and the old top moves down to the right.
        private AvlNode<TKey, TValue> RotateRight(AvlNode<TKey, TValue> top)
        {
            AvlNode<TKey, TValue> newTop = top.Left!;
            top.Left = newTop.Right;
            newTop.Right = top;
            UpdateHeight(top);
            UpdateHeight(newTop);
            return newTop;
        }

        // The right child moves up and the old top moves down to the left.
        private AvlNode<TKey, TValue> RotateLeft(AvlNode<TKey, TValue> top)
        {
            AvlNode<TKey, TValue> newTop = top.Right!;
            top.Right = newTop.Left;
            newTop.Left = top;
            UpdateHeight(top);
            UpdateHeight(newTop);
            return newTop;
        }
    }
}

// References:
// Baeldung, 2026. Red-black tree vs AVL tree. [online] Available at: <https://www.baeldung.com/cs/red-black-tree-vs-avl-tree> [Accessed 11 November 2026].
// Cornell University, 2021. Balanced binary trees. CS 2112 lecture notes. [online] Available at: <https://www.cs.cornell.edu/courses/cs2112/2021fa/lectures/avl> [Accessed 11 November 2026].
// dev.to (christinamcmahon), 2020. Self-Balancing Trees. [online] Available at: <https://dev.to/christinamcmahon/self-balancing-trees-59k9> [Accessed 11 November 2026].
// Morris, J., 1998. Data structures and algorithms: AVL trees. [online] Available at: <https://www.cs.auckland.ac.nz/software/AlgAnim/AVL.html> [Accessed 11 November 2026].
// Rajinikanth, B., 2026. AVL tree datastructure. [online] Available at: <http://btechsmartclass.com/data_structures/avl-trees.html> [Accessed 11 November 2026].