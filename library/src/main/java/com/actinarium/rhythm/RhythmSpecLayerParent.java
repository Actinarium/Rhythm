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

/**
 * An object that can contain other spec layers. Not necessarily a spec layer itself
 *
 * @author Paul Danyliuk
 */
public interface RhythmSpecLayerParent {

    /**
     * Add layer to this parent. Since this is mostly intended for initial configuration, it isn't mandatory that the
     * parent triggers redraw.
     *
     * @param layer Layer to add
     * @return this for chaining
     */
    RhythmSpecLayerParent addLayer(RhythmSpecLayer layer);

}
