/*
 * Copyright (C) 2016 Actinarium
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

package com.actinarium.rhythm;

import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import java.util.Map;

/**
 * An implementation of {@link ArgumentsBundle} that utilizes &ldquo;magic variables&rdquo; mechanism to resolve missing
 * layer arguments: if an argument is not explicitly specified, it tries resolving it from a variable named in a special
 * pattern <code>@{layer_name}_{arg_name}</code> where dashes are replaced with underscores.
 *
 * @author Paul Danyliuk
 */
public class MagicVariablesArgumentsBundle extends SimpleArgumentsBundle {

    protected String mLayerNamePrefix;
    protected Map<String, String> mVariables;

    /**
     * Create a new simple arguments bundle implementation from provided key-&gt;value map.
     *
     * @param arguments A collection that maps arguments to values. In this implementation both the key and the value
     *                  are raw strings, parsed into required data types as requested from the map. The values must be
     *                  already provided as parsable literal values &mdash; this implementation cannot resolve variables
     *                  or calculate expressions.<br>For performance reasons, this map will be used as is, therefore it
     *                  <b>must not</b> be mutated. Furthermore this implementation lacks methods to put new parameters
     *                  into the bag.
     * @param variables A @key-&gt;value map containing magic variables to fall back to
     * @param layerName Layer name as registered in the factory, e.g. <code>grid-lines</code>
     * @param metrics   Display metrics associated with this arguments bundle, required so that dimension values (dp,
     *                  sp
     */
    public MagicVariablesArgumentsBundle(@NonNull Map<String, String> arguments,
                                         @NonNull Map<String, String> variables,
                                         String layerName,
                                         @NonNull DisplayMetrics metrics) {
        super(arguments, metrics);
        mVariables = variables;
        mLayerNamePrefix = '@' + layerName.replace('-', '_') + '_';
    }

    /**
     * {@inheritDoc} If the argument is not declared explicitly, will look up if a matching magic variable is present.
     */
    @Override
    public boolean hasArgument(String key) {
        return mArguments.containsKey(key) || mVariables.containsKey(mLayerNamePrefix + key.replace('-', '_'));
    }

    /**
     * Resolves argument value from the bundle. If the argument is not present in this bundle's arguments map, tries to
     * fall back to a variable with a magic name of <code>@{layer_name}_{arg_name}</code> (concatenated layer and
     * argument names with dashes replaced by underscores).
     *
     * @param key key of the argument whose value to resolve
     * @return string representation of resolved value
     */
    @Override
    protected String resolveArgument(String key) {
        String value = mArguments.get(key);
        if (value == null && !mArguments.containsKey(key)) {
            value = mVariables.get(mLayerNamePrefix + key.replace('-', '_'));
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }
        MagicVariablesArgumentsBundle that = (MagicVariablesArgumentsBundle) o;
        return mVariables.equals(that.mVariables);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mVariables.hashCode();
        return result;
    }
}
