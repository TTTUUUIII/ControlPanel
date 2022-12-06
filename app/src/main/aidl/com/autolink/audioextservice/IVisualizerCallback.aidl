// IVisualizerCallback.aidl
package com.autolink.audioextservice;

// Declare any non-default types here with import statements

interface IVisualizerCallback {
    void onDataChanged(in byte[] sentByte);
}