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
package com.reandroid.apk.xmldecoder;

import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.common.EntryStore;
import com.reandroid.xml.XMLElement;

import java.util.ArrayList;
import java.util.List;

public class XMLBagDecoder {
    private final EntryStore entryStore;
    private final List<BagDecoder> decoderList;
    private final XMLCommonBagDecoder commonBagDecoder;
    public XMLBagDecoder(EntryStore entryStore){
        this.entryStore=entryStore;
        this.decoderList=new ArrayList<>();
        this.decoderList.add(new XMLAttrDecoder(entryStore));
        this.decoderList.add(new XMLPluralsDecoder(entryStore));
        this.decoderList.add(new XMLArrayDecoder(entryStore));
        this.commonBagDecoder = new XMLCommonBagDecoder(entryStore);
    }
    public void decode(ResTableMapEntry mapEntry, XMLElement parentElement){
        BagDecoder bagDecoder=getFor(mapEntry);
        bagDecoder.decode(mapEntry, parentElement);
    }
    private BagDecoder getFor(ResTableMapEntry mapEntry){
        for(BagDecoder bagDecoder:decoderList){
            if(bagDecoder.canDecode(mapEntry)){
                return bagDecoder;
            }
        }
        return commonBagDecoder;
    }
}
