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
package com.reandroid.apk;

import com.reandroid.archive.InputSource;
import com.reandroid.archive.block.ApkSignatureBlock;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.identifiers.PackageIdentifier;
import com.reandroid.identifiers.TableIdentifier;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ApkModuleDecoder extends ApkModuleCoder{
    private final ApkModule apkModule;
    private final Set<String> mDecodedPaths;
    private DexDecoder mDexDecoder;
    private boolean mLogErrors;
    private DecodeFilter mDecodeFilter;

    public ApkModuleDecoder(ApkModule apkModule){
        super();
        this.apkModule = apkModule;
        this.mDecodedPaths = new HashSet<>();
        setApkLogger(apkModule.getApkLogger());
    }
    public final void decode(File mainDirectory) throws IOException{
        initialize();
        decodeUncompressedFiles(mainDirectory);
        decodeAndroidManifest(mainDirectory);
        decodeResourceTable(mainDirectory);
        decodeDexFiles(mainDirectory);
        extractRootFiles(mainDirectory);
        decodePathMap(mainDirectory);
        dumpSignatures(mainDirectory);
    }
    public abstract void decodeResourceTable(File mainDirectory) throws IOException;
    abstract void decodeAndroidManifest(File mainDirectory) throws IOException;

    public void extractRootFiles(File mainDirectory) throws IOException {
        logMessage("Extracting root files ...");
        File rootDir = new File(mainDirectory, ApkUtil.ROOT_NAME);
        for(InputSource inputSource:apkModule.getInputSources()){
            if(containsDecodedPath(inputSource.getAlias())){
                continue;
            }
            extractRootFile(rootDir, inputSource);
            addDecodedPath(inputSource.getAlias());
        }
    }
    public void decodeUncompressedFiles(File mainDirectory)
            throws IOException {
        File file = new File(mainDirectory, UncompressedFiles.JSON_FILE);
        logMessage("Decode: " + file.getName());
        UncompressedFiles uncompressedFiles = new UncompressedFiles();
        uncompressedFiles.addCommonExtensions();
        uncompressedFiles.addPath(getApkModule().getZipEntryMap());
        uncompressedFiles.toJson().write(file);
    }
    public void decodeDexFiles(File mainDir) throws IOException {
        List<DexFileInputSource> dexList = getApkModule().listDexFiles();
        decodeDexFiles(dexList, mainDir);
    }
    public void decodeDexFiles(List<DexFileInputSource> dexList, File mainDir) throws IOException {
        for(DexFileInputSource dexFileInputSource : dexList){
            decodeDexFile(dexFileInputSource, mainDir);
        }
    }
    public void decodeDexFile(DexFileInputSource dexFileInputSource, File mainDir) throws IOException {
        String path = dexFileInputSource.getAlias();
        DexDecoder dexDecoder = getDexDecoder();
        boolean decoded = dexDecoder.decodeDex(dexFileInputSource, mainDir);
        if(decoded){
            addDecodedPath(path);
        }
    }
    @Override
    public ApkModule getApkModule() {
        return apkModule;
    }
    public DexDecoder getDexDecoder() {
        if(mDexDecoder == null){
            DexFileRawDecoder rawDecoder = new DexFileRawDecoder();
            rawDecoder.setApkLogger(getApkLogger());
            mDexDecoder = rawDecoder;
        }
        return mDexDecoder;
    }
    public void setDexDecoder(DexDecoder dexDecoder) {
        this.mDexDecoder = dexDecoder;
    }

    public void sanitizeFilePaths(){
        PathSanitizer sanitizer = PathSanitizer.create(getApkModule());
        sanitizer.sanitize();
    }
    public void dumpSignatures(File mainDirectory) throws IOException {
        ApkModule apkModule = getApkModule();
        ApkSignatureBlock signatureBlock = apkModule.getApkSignatureBlock();
        if(signatureBlock == null){
            return;
        }
        File sigDir = new File(mainDirectory, ApkUtil.SIGNATURE_DIR_NAME);
        logMessage("Dumping signatures ...");
        signatureBlock.writeSplitRawToDirectory(sigDir);
    }
    public void decodePathMap(File mainDirectory) throws IOException {
        File file = new File(mainDirectory, PathMap.JSON_FILE);
        PathMap pathMap = new PathMap();
        pathMap.add(getApkModule().getZipEntryMap());
        pathMap.toJson().write(file);
    }
    public boolean containsDecodedPath(String path){
        return mDecodedPaths.contains(path);
    }
    public void addDecodedPath(String path){
        mDecodedPaths.add(path);
    }

    public DecodeFilter getDecodeFilter(){
        if(mDecodeFilter == null){
            mDecodeFilter = new DecodeFilter();
        }
        return mDecodeFilter;
    }
    public void setDecodeFilter(DecodeFilter decodeFilter) {
        this.mDecodeFilter = decodeFilter;
    }
    boolean isExcluded(String path){
        return getDecodeFilter().isExcluded(path);
    }

    private void extractRootFile(File rootDir, InputSource inputSource) throws IOException {
        File file = inputSource.toFile(rootDir);
        inputSource.write(file);
    }

    void logOrThrow(String message, Throwable exception) throws IOException{
        if(isLogErrors()){
            logError(message, exception);
            return;
        }
        if(message == null && exception == null){
            return;
        }
        if(exception == null){
            exception = new IOException(message);
        }
        if(exception instanceof IOException){
            throw (IOException) exception;
        }
        throw new IOException(exception);
    }


    public void validateResourceNames(){
        logMessage("Validating resource names ...");
        TableBlock tableBlock = apkModule.getTableBlock();
        TableIdentifier tableIdentifier = new TableIdentifier();
        tableIdentifier.load(tableBlock);
        String msg = tableIdentifier.validateSpecNames();
        if(msg == null){
            logMessage("All resource names are valid");
            return;
        }
        int removed = tableBlock.removeUnusedSpecs();
        msg = msg + ", removed specs = " + removed;
        logMessage(msg);
    }
    public void validateResourceNames(PackageBlock packageBlock){
        logMessage("Validating: " + packageBlock.getName());
        PackageIdentifier packageIdentifier = new PackageIdentifier();
        packageIdentifier.load(packageBlock);
        String msg = packageIdentifier.validateSpecNames();
        if(msg == null){
            logMessage("[" + packageBlock.getName() + "] All resource names are valid");
            return;
        }
        int removed = packageBlock.removeUnusedSpecs();
        msg = "[" + packageBlock.getName() + "]" + msg + ", removed specs = " + removed;
        logMessage(msg);
    }
    void initialize(){
        mDecodedPaths.clear();
    }

    public boolean isLogErrors() {
        return mLogErrors;
    }
    public void setLogErrors(boolean logErrors) {
        this.mLogErrors = logErrors;
    }


    static File toPackageDirectory(File mainDir, PackageBlock packageBlock){
        File dir = new File(mainDir, TableBlock.DIRECTORY_NAME);
        return new File(dir, packageBlock.buildDecodeDirectoryName());
    }
}
