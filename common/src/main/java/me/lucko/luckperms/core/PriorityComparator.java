/*
 * Copyright (c) 2016 Lucko (Luck) <luck@lucko.me>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.lucko.luckperms.api.Node;

import java.util.Comparator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PriorityComparator implements Comparator<Node> {
    private static final PriorityComparator instance = new PriorityComparator();

    public static Comparator<Node> get() {
        return instance;
    }

    public static Comparator<Node> reverse() {
        return instance.reversed();
    }

    @Override
    public int compare(Node o1, Node o2) {
        if (o1.equals(o2)) {
            return 0;
        }

        if (o1.isOverride() != o2.isOverride()) {
            return o1.isOverride() ? 1 : -1;
        }

        if (o1.isServerSpecific() != o2.isServerSpecific()) {
            return o1.isServerSpecific() ? 1 : -1;
        }

        if (o1.isWorldSpecific() != o2.isWorldSpecific()) {
            return o1.isWorldSpecific() ? 1 : -1;
        }

        if (o1.isTemporary() != o2.isTemporary()) {
            return o1.isTemporary() ? 1 : -1;
        }

        if (o1.isWildcard() != o2.isWildcard()) {
            return o1.isWildcard() ? 1 : -1;
        }

        if (o1.isTemporary()) {
            return o1.getSecondsTilExpiry() < o2.getSecondsTilExpiry() ? 1 : -1;
        }

        if (o1.isWildcard()) {
            return o1.getWildcardLevel() > o2.getWildcardLevel() ? 1 : -1;
        }

        return 1;
    }
}
