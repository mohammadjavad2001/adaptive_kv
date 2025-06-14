// Source code is decompiled from a .class file using FernFlower decompiler.
package de.learnlib.ds;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import net.automatalib.graphs.Graph;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

public interface RecursiveADSNode<S, I, O, N extends RecursiveADSNode<S, I, O, N>> extends Graph<N, N> {
   @Pure
   @Nullable I getSymbol();

   void setSymbol(I var1);

   @Pure
   @Nullable N getParent();

   void setParent(N var1);

   default Collection<N> getNodesForRoot(N root) {
      List<N> result = new ArrayList();
      Queue<N> queue = new ArrayDeque();
      queue.add(root);

      while(!queue.isEmpty()) {
         N node = (N) queue.poll();
         result.add(node);
         queue.addAll(node.getChildren().values());
      }

      return Collections.unmodifiableList(result);
   }

   Map<O, N> getChildren();

   default Collection<N> getOutgoingEdges(N node) {
      return Collections.unmodifiableCollection(node.getChildren().values());
   }

   default N getTarget(N edge) {
      return edge;
   }

   default VisualizationHelper<N, N> getVisualizationHelper() {
      return new DefaultVisualizationHelper<>();
   }

   boolean isLeaf();

   @Nullable S getState();

   void setState(S var1);
}
