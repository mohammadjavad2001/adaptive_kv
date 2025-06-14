/* Copyright (C) 2013-2024 TU Dortmund University
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.ds;

import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Reset node implementation.
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 */
public class ADTResetNode<S, I, O> implements ADTNode<S, I, O> {

    private ADTNode<S, I, O> parent;
    private final Map<O, ADTNode<S, I, O>> children;

    public ADTResetNode(ADTNode<S, I, O> child) {
        this.children = new HashMap<>();
        if (child != null) {
            this.children.put(null, child);
            child.setParent(this);
        }
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.RESET_NODE;
    }

    @Override
    public S getState() {
        return null;
    }

    @Override
    public S getHypothesisState() {
        return null;
    }

    @Override
    public void setHypothesisState(S state) {
        // Reset nodes don't have hypothesis states
    }

    @Override
    public I getSymbol() {
        return null;
    }

    @Override
    public void setSymbol(I symbol) {
        // Reset nodes don't have symbols
    }

    @Override
    public ADTNode<S, I, O> getParent() {
        return parent;
    }

    @Override
    public void setParent(ADTNode<S, I, O> parent) {
        this.parent = parent;
    }

    @Override
    public Map<O, ADTNode<S, I, O>> getChildren() {
        return children;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
} 