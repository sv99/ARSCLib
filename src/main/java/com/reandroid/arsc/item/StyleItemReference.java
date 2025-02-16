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
package com.reandroid.arsc.item;

import com.reandroid.arsc.base.Block;

class StyleItemReference implements WeakStringReference{
    private final StyleItem styleItem;
    private final int stylePiece;
    StyleItemReference(StyleItem styleItem, int stylePiece){
        this.styleItem = styleItem;
        this.stylePiece = stylePiece;
    }
    @Override
    public void set(int val) {
        styleItem.setStringRef(stylePiece, val);
    }
    @Override
    public int get() {
        return styleItem.getStringRef(stylePiece);
    }
    @SuppressWarnings("unchecked")
    @Override
    public <T1 extends Block> T1 getReferredParent(Class<T1> parentClass){
        if(parentClass.isInstance(styleItem)){
            return (T1) styleItem;
        }
        return styleItem.getParentInstance(parentClass);
    }
    @Override
    public boolean equals(Object obj){
        if(obj==this){
            return true;
        }
        if(!(obj instanceof StyleItemReference)){
            return false;
        }
        StyleItemReference other = (StyleItemReference) obj;
        return stylePiece == other.stylePiece
                && styleItem == other.styleItem;
    }
    @Override
    public int hashCode(){
        return stylePiece;
    }
}
