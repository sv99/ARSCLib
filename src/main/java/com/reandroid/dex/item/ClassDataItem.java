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
package com.reandroid.dex.item;

import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class ClassDataItem extends FixedBlockContainer implements SmaliFormat {
    private final IntegerReference offsetReference;

    private final Ule128Item staticFieldsCount;
    private final Ule128Item instanceFieldCount;
    private final Ule128Item directMethodCount;
    private final Ule128Item virtualMethodCount;

    private final FieldDefArray staticFields;
    private final FieldDefArray instanceFields;
    private final MethodDefArray directMethods;
    private final MethodDefArray virtualMethods;


    public ClassDataItem(IntegerReference offsetReference) {
        super(8);
        this.offsetReference = offsetReference;
        this.staticFieldsCount = new Ule128Item();
        this.instanceFieldCount = new Ule128Item();
        this.directMethodCount = new Ule128Item();
        this.virtualMethodCount = new Ule128Item();


        this.staticFields = new FieldDefArray(staticFieldsCount);
        this.instanceFields = new FieldDefArray(instanceFieldCount);
        this.directMethods = new MethodDefArray(directMethodCount);
        this.virtualMethods = new MethodDefArray(virtualMethodCount);

        staticFields.setParent(this);
        instanceFields.setParent(this);

        addChild(0, staticFieldsCount);
        addChild(1, instanceFieldCount);
        addChild(2, directMethodCount);
        addChild(3, virtualMethodCount);

        addChild(4, staticFields);
        addChild(5, instanceFields);
        addChild(6, directMethods);
        addChild(7, virtualMethods);
    }

    public int getStaticFieldsCount() {
        return staticFieldsCount.get();
    }
    public int getInstanceFieldCount() {
        return instanceFieldCount.get();
    }
    public int getDirectMethodCount() {
        return directMethodCount.get();
    }
    public int getVirtualMethodCount() {
        return virtualMethodCount.get();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        int offset = offsetReference.get();
        if(offset <= 0){
            return;
        }
        int position = reader.getPosition();
        reader.seek(offset);
        super.onReadBytes(reader);
        reader.seek(position);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        staticFields.append(writer);
        instanceFields.append(writer);
        directMethods.append(writer);
        virtualMethods.append(writer);
    }
    @Override
    public String toString() {
        return "staticFieldsCount=" + getStaticFieldsCount() +
                ", instanceFieldCount=" + getInstanceFieldCount() +
                ", directMethodCount=" + getDirectMethodCount() +
                ", virtualMethodCount=" + getVirtualMethodCount();
    }
}
