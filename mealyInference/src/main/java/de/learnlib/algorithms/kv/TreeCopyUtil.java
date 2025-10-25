package de.learnlib.algorithms.kv;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import de.learnlib.datastructure.discriminationtree.MultiDTNode;

// import de.learnlib.ds.MultiDTree;
// import de.learnlib.ds.MultiDTNode;
// import de.learnlib.ds.StateInfo;
import de.learnlib.algorithms.kv.StateInfo;

import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
// import de.learnlib.ds.AbstractWordBasedDTNode;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.words.Word;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for deep copying discrimination trees
 */
public class TreeCopyUtil {

    /**
     * Creates a deep copy of a MultiDTree discrimination tree.
     * This creates a completely independent copy that won't be affected by changes to the original.
     */
    public static MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> 
            deepCopyTree(MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> originalTree) {
        
        if (originalTree == null) {
            return null;
        }

        // Get the root node from original tree
        AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> originalRoot = 
            originalTree.getRoot();

        // Create mapping to track copied StateInfo objects (to maintain references)
        Map<StateInfo<String, Word<Word<String>>>, StateInfo<String, Word<Word<String>>>> stateInfoMap = 
            new HashMap<>();

        // Get oracle from original tree (needed for tree operations)
        de.learnlib.api.oracle.MembershipOracle<String, Word<Word<String>>> oracle = getOracleFromTree(originalTree);
        
        System.out.println("Deep copy: Oracle extracted = " + (oracle != null ? "SUCCESS" : "NULL"));

        // Recursively copy the root node and all its descendants
        MultiDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> copiedRoot = 
            copyNode(originalRoot, stateInfoMap);
            
        System.out.println("Deep copy: Root node copied, is leaf = " + copiedRoot.isLeaf());

        // Create new tree with copied root and original oracle
        MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> copiedTree = 
            new MultiDTree<>(copiedRoot.getData(), oracle);

        // Set the root to our copied structure
        // Note: We need to replace the root that was created in constructor
        setTreeRoot(copiedTree, copiedRoot);
        
        System.out.println("Deep copy: Tree created and root set successfully");

        return copiedTree;
    }

    /**
     * Recursively copies a node and all its children
     */
    private static MultiDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> 
            copyNode(AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> original,
                     Map<StateInfo<String, Word<Word<String>>>, StateInfo<String, Word<Word<String>>>> stateInfoMap) {
        
        // Create new node with copied data
        StateInfo<String, Word<Word<String>>> copiedData = null;
        if (original.isLeaf() && original.getData() != null) {
            copiedData = copyStateInfo(original.getData(), stateInfoMap);
        }

        MultiDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> copiedNode = 
            new MultiDTNode<>(copiedData);

        // Update the StateInfo's dtNode reference to point to the new node
        if (copiedData != null) {
            copiedData.dtNode = copiedNode;
        }

        // If it's not a leaf, copy the discriminator and children
        if (!original.isLeaf()) {
            // Copy discriminator (Word is immutable, so we can reuse)
            copiedNode.setDiscriminator(original.getDiscriminator());

            // Copy all children
            Map<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>> 
                copiedChildren = new HashMap<>();

            for (Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>> 
                    entry : original.getChildEntries()) {
                
                Word<Word<String>> outcome = entry.getKey();
                AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> childNode = 
                    entry.getValue();

                // Recursively copy child
                MultiDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> copiedChild = 
                    copyNode(childNode, stateInfoMap);
                
                // Set parent reference manually
                setParentReference(copiedChild, copiedNode, outcome);
                
                copiedChildren.put(outcome, copiedChild);
            }

            // Replace children
            copiedNode.replaceChildren(copiedChildren);
        }

        return copiedNode;
    }

    /**
     * Copies StateInfo object
     */
    private static StateInfo<String, Word<Word<String>>> 
            copyStateInfo(StateInfo<String, Word<Word<String>>> original,
                         Map<StateInfo<String, Word<Word<String>>>, StateInfo<String, Word<Word<String>>>> stateInfoMap) {
        
        // Check if already copied
        if (stateInfoMap.containsKey(original)) {
            return stateInfoMap.get(original);
        }

        // Create new StateInfo (Word is immutable so we can reuse accessSequence)
        StateInfo<String, Word<Word<String>>> copied = 
            new StateInfo<>(original.id, original.accessSequence);

        // Copy incoming transitions list using reflection (fetchIncoming is destructive)
        try {
            java.lang.reflect.Field incomingField = StateInfo.class.getDeclaredField("incoming");
            incomingField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<Long> originalIncoming = (java.util.List<Long>) incomingField.get(original);
            
            if (originalIncoming != null && !originalIncoming.isEmpty()) {
                for (Long incoming : originalIncoming) {
                    int sourceState = (int) (incoming >> Integer.SIZE);
                    int transIdx = incoming.intValue();
                    copied.addIncoming(sourceState, transIdx);
                }
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not copy incoming transitions: " + e.getMessage());
        }

        stateInfoMap.put(original, copied);
        return copied;
    }

    /**
     * Uses reflection to get the oracle from a tree
     */
    private static MembershipOracle<String, Word<Word<String>>> getOracleFromTree(
            MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree) {
        try {
            java.lang.reflect.Field oracleField = tree.getClass().getSuperclass().getSuperclass().getDeclaredField("oracle");
            oracleField.setAccessible(true);
            @SuppressWarnings("unchecked")
            MembershipOracle<String, Word<Word<String>>> oracle = 
                (MembershipOracle<String, Word<Word<String>>>) oracleField.get(tree);
            return oracle;
        } catch (Exception e) {
            System.out.println("Warning: Could not get oracle from tree: " + e.getMessage());
            return null;
        }
    }

    /**
     * Uses reflection to set the root of a tree (since there's no public setter)
     */
    private static void setTreeRoot(MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree,
                                    MultiDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> newRoot) {
        try {
            java.lang.reflect.Field rootField = tree.getClass().getSuperclass().getSuperclass().getDeclaredField("root");
            rootField.setAccessible(true);
            rootField.set(tree, newRoot);
        } catch (Exception e) {
            System.err.println("Warning: Could not set tree root via reflection: " + e.getMessage());
        }
    }

    /**
     * Uses reflection to set parent reference in a node
     */
    private static void setParentReference(MultiDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> child,
                                           MultiDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> parent,
                                           Word<Word<String>> outcome) {
        try {
            java.lang.reflect.Field parentField = child.getClass().getSuperclass().getSuperclass().getDeclaredField("parent");
            parentField.setAccessible(true);
            parentField.set(child, parent);

            java.lang.reflect.Field outcomeField = child.getClass().getSuperclass().getSuperclass().getDeclaredField("parentOutcome");
            outcomeField.setAccessible(true);
            outcomeField.set(child, outcome);

            java.lang.reflect.Field depthField = child.getClass().getSuperclass().getSuperclass().getDeclaredField("depth");
            depthField.setAccessible(true);
            depthField.set(child, parent.getDepth() + 1);
        } catch (Exception e) {
            System.err.println("Warning: Could not set parent reference via reflection: " + e.getMessage());
        }
    }
}
