/*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.arsc.array;

import com.reandroid.arsc.value.EntryHeaderMap;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.arsc.value.ResValueMap;

public class ResValueMapArray extends CompoundItemArray<ResValueMap> {
    public ResValueMapArray(){
        super();
    }
    @Override
    public ResValueMap createNext(){
        ResValueMap resValueMap = super.createNext();
        EntryHeaderMap headerMap = getEntryHeaderMap();
        headerMap.setValuesCount(childesCount());
        return resValueMap;
    }
    private EntryHeaderMap getEntryHeaderMap(){
        ResTableMapEntry mapEntry = getParent(ResTableMapEntry.class);
        assert mapEntry != null;
        return mapEntry.getHeader();
    }
    @Override
    public ResValueMap newInstance() {
        return new ResValueMap();
    }

    @Override
    public ResValueMap[] newInstance(int len) {
        return new ResValueMap[len];
    }
}
