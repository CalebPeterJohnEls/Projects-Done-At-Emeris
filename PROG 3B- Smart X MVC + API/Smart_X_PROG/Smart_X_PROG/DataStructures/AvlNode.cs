namespace Smart_X_API.DataStructures
{
    // One node of the AVL tree. It holds a key (the time), a value (the reading),
    // a left child and a right child. The node also stores its own height, so the
    // tree can quickly work out if one side has become too tall (Rajinikanth, 2026).
    // "where TKey : IComparable<TKey>" means the key must be something that can be
    // compared, like a DateTime (Microsoft, 2026).
    public class AvlNode<TKey, TValue> where TKey : IComparable<TKey>
    {
        public TKey Key { get; set; }
        public TValue Value { get; set; }
        public AvlNode<TKey, TValue>? Left { get; set; }
        public AvlNode<TKey, TValue>? Right { get; set; }

        // A new node is a leaf, so its height starts at 1.
        public int Height { get; set; } = 1;

        public AvlNode(TKey key, TValue value)
        {
            Key = key;
            Value = value;
        }
    }
}

// References:
// Microsoft, 2026. Constraints on type parameters (C# programming guide). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/csharp/programming-guide/generics/constraints-on-type-parameters> [Accessed 11 November 2026].
// Rajinikanth, B., 2026. AVL tree datastructure. [online] Available at: <http://btechsmartclass.com/data_structures/avl-trees.html> [Accessed 11 November 2026].