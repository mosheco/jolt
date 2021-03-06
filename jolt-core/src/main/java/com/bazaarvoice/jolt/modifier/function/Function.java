/*
 * Copyright 2013 Bazaarvoice, Inc.
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

package com.bazaarvoice.jolt.modifier.function;

import com.bazaarvoice.jolt.common.Optional;

/**
 * Modifier supports a Function on RHS that accepts jolt path expressions as arguments and evaluates
 * them at runtime before calling it. Function always returns an Optional, and the value is written
 * only if the optional is not empty.
 *
 * function spec is defined by "key": "=functionName(args...)"
 *
 *
 * input:
 *      { "num": -1.0 }
 * spec:
 *      { "num": "=abs(@(1,&0))" }
 *      will call the stock function Math.abs() and will pass the matching value at "num"
 *
 * spec:
 *      { "num": "=abs" }
 *      an alternative shortcut will do the same thing
 *
 * output:
 *      { "num": 1.0 }
 *
 *
 *
 * input:
 *      { "value": -1.0 }
 *
 * spec:
 *      { "absValue": "=abs(@(1,value))" }
 *      will evaluate the jolt path expression @(1,value) and pass the output to stock function Math.abs()
 *
 * output:
 *      { "value": -1.0, "absValue": 1.0 }
 *
 *
 *
 * Currently defined stock functions are:
 *
 *      toLower     - returns toLower value of toString() value of first arg, rest is ignored
 *      toUpper     - returns toUpper value of toString() value of first arg, rest is ignored
 *      concat      - concatenate all given arguments' toString() values
 *
 *      min       - returns the min of all numbers provided in the arguments, non-numbers are ignored
 *      max       - returns the max of all numbers provided in the arguments, non-numbers are ignored
 *      abs         - returns the absolute value of first argument, rest is ignored
 *      toInteger   - returns the intValue() value of first argument if its numeric, rest is ignored
 *      toDouble    - returns the doubleValue() value of first argument if its numeric, rest is ignored
 *      toLong      - returns the longValue() value of first argument if its numeric, rest is ignored
 *
 * All of these functions returns Optional.EMPTY if unsuccessful, which results in a no-op when performing
 * the actual write in the json doc.
 *
 * i.e.
 * input:
 *      { "value1": "xyz" } --- note: string, not number
 *      { "value1": "1.0" } --- note: string, not number
 *
 * spec:
 *      { "value1": "=abs" } --- fails silently
 *      { "value2": "=abs" }
 *
 * output:
 *      { "value1": "xyz", "value2": "1" } --- note: "absValue": null is not inserted
 *
 *
 * This is work in progress, and probably will be changed in future releases. Hence it is marked for
 * removal as it'll eventually be moved to a different package as the Function feature is baked into
 * other transforms as well. In short this interface is not yet ready to be implemented outside jolt!
 *
 */

@Deprecated
public interface Function {

    public Optional<Object> apply(Object... args);

    /**
     * Does nothing
     *
     * spec - "key": "=noop"
     *
     * will cause the key to remain unchanged
     */
    public static final Function noop = new Function() {
        @Override
        public Optional<Object> apply( final Object... args ) {
            return Optional.empty();
        }
    };

    /**
     * Returns the first argument, null or otherwise
     *
     * spec - "key": [ "=isPresent", "otherValue" ]
     *
     * input - "key": null
     * output - "key": null
     *
     * input - "key": "value"
     * output - "key": "value"
     *
     * input - key is missing
     * output - "key": "otherValue"
     *
     */
    public static final Function isPresent = new Function() {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if (args.length == 0) {
                return Optional.empty();
            }
            return Optional.of( args[0] );
        }
    };

    /**
     * Returns the first argument if in not null
     *
     * spec - "key": ["=notNull", "otherValue" ]
     *
     * input - "key": null
     * output - "key": "otherValue"
     *
     * input - "key": "value"
     * output - "key": "value"
     *
     */
    public static final Function notNull = new Function() {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if (args.length == 0 || args[0] == null) {
                return Optional.empty();
            }
            return Optional.of( args[0] );
        }
    };

    /**
     * Returns the first argument if it is null
     *
     * spec - "key": ["=inNull", "otherValue" ]
     *
     * input - "key": null
     * output - "key": null
     *
     * input - "key": "value"
     * output - "key": "otherValue"
     *
     */
    public static final Function isNull = new Function() {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if (args.length == 0 || args[0] != null) {
                return Optional.empty();
            }
            return Optional.of( args[0] );
        }
    };
}
