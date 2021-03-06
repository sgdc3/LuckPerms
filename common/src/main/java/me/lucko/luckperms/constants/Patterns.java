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

package me.lucko.luckperms.constants;

import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@UtilityClass
public class Patterns {
    private static final Map<String, Pattern> CACHE = new ConcurrentHashMap<>();

    public static final Pattern SPACE = Pattern.compile(" ");
    public static final Pattern SERVER_DELIMITER = Pattern.compile("\\/");
    public static final Pattern WORLD_DELIMITER = Pattern.compile("\\-");
    public static final Pattern TEMP_DELIMITER = Pattern.compile("\\$");
    public static final Pattern DOT = Pattern.compile("\\.");
    public static final Pattern VERTICAL_BAR = Pattern.compile("\\|");
    public static final Pattern GROUP_MATCH = Pattern.compile("group\\..*");
    public static final Pattern NON_ALPHA_NUMERIC = Pattern.compile("[\\/\\$\\.\\-]");
    public static final Pattern NON_USERNAME = Pattern.compile("[^A-Za-z0-9_]");
    public static final Pattern SHORTHAND_NODE = Pattern.compile("\\.\\([^.]+\\)");
    public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf('§') + "[0-9A-FK-OR]");
    public static final Pattern NODE_CONTEXTS = Pattern.compile("\\(.+\\).*");

    public static Pattern compile(String regex) throws PatternSyntaxException {
        if (!CACHE.containsKey(regex)) {
            Pattern p;
            try {
                p = Pattern.compile(regex);
            } catch (PatternSyntaxException e) {
                return null;
            }

            CACHE.put(regex, p);
        }

        return CACHE.get(regex);
    }

}
